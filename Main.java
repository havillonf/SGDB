import java.io.*;
import java.util.*;

class Record {
    int year;
    double value;

    public Record(int year, double value) {
        this.year = year;
        this.value = value;
    }
}

class HashBucket {
    List<Record> records;
    int localDepth;

    public HashBucket(int depth) {
        this.localDepth = depth;
        this.records = new ArrayList<>();
    }

    public boolean isFull() {
        return records.size() >= 3;
    }

    public void insertRecord(Record record) {
        if (!isFull()) {
            records.add(record);
        }
    }
}

class ExtendibleHashIndex {
    List<HashBucket> directory;
    int globalDepth;

    public ExtendibleHashIndex(int depth) {
        this.globalDepth = depth;
        this.directory = new ArrayList<>(1 << depth);
        for (int i = 0; i < (1 << depth); i++) {
            this.directory.add(new HashBucket(depth));
        }
    }

    private int hash(int key) {
        return key & ((1 << globalDepth) - 1);
    }

    public void insert(Record record, PrintWriter output) {
        int index = hash(record.year);
        HashBucket bucket = directory.get(index);

        if (bucket.isFull()) {
            // Handle bucket splitting and directory duplication if necessary
            output.println("DUP DIR:/<" + globalDepth + ">,<" + bucket.localDepth + ">");
        } else {
            bucket.insertRecord(record);
            output.println("INC:" + record.year + "/" + globalDepth + "," + bucket.localDepth);
        }
    }

    public void search(int key, PrintWriter output) {
        int index = hash(key);
        HashBucket bucket = directory.get(index);
        int count = 0;
        for (Record record : bucket.records) {
            if (record.year == key) {
                count++;
            }
        }
        output.println("BUS:" + key + "/" + count);
    }

    public void delete(int key, PrintWriter output) {
        int index = hash(key);
        HashBucket bucket = directory.get(index);
        int initialSize = bucket.records.size();
        bucket.records.removeIf(record -> record.year == key);
        int removed = initialSize - bucket.records.size();
        output.println("REM:" + key + "/" + removed + "," + globalDepth + "," + bucket.localDepth);
    }
}

public class Main {
    public static void main(String[] args) throws IOException {
        ExtendibleHashIndex ehi = new ExtendibleHashIndex(3); // Placeholder for depth initialization
        loadComprasCsv(ehi, "compras.csv");

        File inputFile = new File("in.txt");
        File outputFile = new File("out.txt");
        Scanner scanner = new Scanner(inputFile);
        PrintWriter output = new PrintWriter(outputFile);

        // Read initial global depth from the first line of in.txt
        String firstLine = scanner.nextLine();
        int globalDepth = Integer.parseInt(firstLine.split("/")[1]);
        ehi.globalDepth = globalDepth; // Set the global depth from in.txt

        output.println(firstLine); // Echo the first line to out.txt

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] parts = line.split(":");
            String command = parts[0];
            int key = Integer.parseInt(parts[1]);

            switch (command) {
                case "INC":
                    ehi.insert(new Record(key, 0.0), output); // Assuming dummy value
                    break;
                case "REM":
                    ehi.delete(key, output);
                    break;
                case "BUS":
                    ehi.search(key, output);
                    break;
            }
        }

        // Print the final global depth
        output.println("P:/" + ehi.globalDepth);
        scanner.close();
        output.close();
    }

    private static void loadComprasCsv(ExtendibleHashIndex ehi, String fileName) throws FileNotFoundException {
        Scanner csvScanner = new Scanner(new File(fileName));
        while (csvScanner.hasNextLine()) {
            String line = csvScanner.nextLine();
            String[] parts = line.split(",");
            int year = Integer.parseInt(parts[0]);
            double value = Double.parseDouble(parts[1]);
            ehi.insert(new Record(year, value), new PrintWriter(System.out)); // Output to System.out for initial load
        }
        csvScanner.close();
    }
}
