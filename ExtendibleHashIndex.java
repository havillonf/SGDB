import java.io.*;
import java.util.ArrayList;
import java.util.List;

class ExtendibleHashIndex {
    List<HashBucket> directory;
    int globalDepth;

    public ExtendibleHashIndex(int depth) throws IOException {
        this.globalDepth = depth;
        this.directory = new ArrayList<>(1 << depth);
        for (int i = 0; i < (1 << depth); i++) {
            this.directory.add(new HashBucket(depth, i));
        }
    }

    private int hash(int year) {
        return year & ((1 << globalDepth) - 1);
    }

    public void insert(Record record, PrintWriter output) throws IOException {
        System.out.println("Inserting " + record.year);
        int index = hash(record.year);
        HashBucket bucket = directory.get(index);

        if (bucket.isFull()) {
            splitBucket(index);

            insert(record, output); // Try to insert again after splitting
            output.println("DUP DIR:/<" + globalDepth + ">,<" + bucket.localDepth + ">");
        } else {
            bucket.insertRecord(record);
            output.println("INC:" + record.year + "/" + globalDepth + "," + bucket.localDepth);
        }
    }

    private void splitBucket(int index) throws IOException {
        HashBucket oldBucket = directory.get(index);

        if(oldBucket.isFull()) {
            int currentSize = directory.size();
            for (int i = 0; i < currentSize; i++) {
                //add the new duplicate
                HashBucket newBucket = new HashBucket(oldBucket.localDepth + 1, (int) (i + Math.pow(2, globalDepth)));
                directory.add(newBucket);
            }
            globalDepth++;
        }

        int newLocalDepth = oldBucket.localDepth + 1;
        HashBucket newBucket = directory.get((int) (index + Math.pow(2, globalDepth-1)));

        // Create a temporary list to hold records while we clear the old bucket
        File tempFile = new File("temp_records.txt");
        try (BufferedReader reader = new BufferedReader(new FileReader(oldBucket.bucketFile));
             PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.println(line); // Write to temp file
            }
        }
        oldBucket.clear();

        try (BufferedReader tempReader = new BufferedReader(new FileReader(tempFile))) {
            String line;
            while ((line = tempReader.readLine()) != null) {
                String[] parts = line.split(",");
                int idx = Integer.parseInt(parts[0]);
                int year = Integer.parseInt(parts[1]);
                Record record = new Record(idx, year);
                int newIndex = hash(record.year);
                if (newIndex == index) {
                    oldBucket.insertRecord(record);
                } else {
                    newBucket.insertRecord(record);
                }
            }
        }

        tempFile.delete(); // Cleanup the temporary file

        oldBucket.localDepth = newLocalDepth; // Update the local depth of the old bucket
    }

    public void search(int year, PrintWriter output) throws IOException {
        int index = hash(year);
        HashBucket bucket = directory.get(index);
        int count = bucket.search(year);
        output.println("BUS=:" + year + "/" + count);
    }

    public void delete(int year, PrintWriter output) throws IOException {
        int index = hash(year);
        HashBucket bucket = directory.get(index);
        int removed = bucket.delete(year);
        output.println("REM:" + year + "/" + removed + "," + globalDepth + "," + bucket.localDepth);
    }
}