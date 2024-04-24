import java.io.*;
import java.util.ArrayList;
import java.util.List;

class HashBucket {
    File bucketFile;
    int localDepth;
    int maxEntries = 3;

    public HashBucket(int depth, int bucketId) {
        this.localDepth = depth;
        String bits = Main.lastBits(bucketId, depth);
        this.bucketFile = new File("buckets/" + bits + ".txt");
    }

    public boolean isFull() throws IOException {
        if (!bucketFile.exists()) {
            return false;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(bucketFile))) {
            int count = 0;
            while (br.readLine() != null) {
                count++;
            }
            return count >= maxEntries;
        }
    }

    public void insertRecord(Record record) throws IOException {
        if (!bucketFile.exists()) {
            bucketFile.createNewFile(); // Ensure the file exists before trying to write to it.
        }
        try (FileWriter fw = new FileWriter(bucketFile, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(record.toString());
        }
    }

    public int search(int key) throws IOException {
        if (!bucketFile.exists()) {
            return 0; // If the file doesn't exist, there are no records to find.
        }
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(bucketFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                Record record = parseRecord(line);
                if (record.year == key) {
                    count++;
                }
            }
        }
        return count;
    }

    public int delete(int key) throws IOException {
        if (!bucketFile.exists()) {
            return 0; // If the file doesn't exist, there's nothing to delete.
        }
        File tempFile = new File(bucketFile.getAbsolutePath() + ".tmp");
        int removedCount = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(bucketFile));
             BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                Record record = parseRecord(line);
                if (record.year != key) {
                    bw.write(line);
                    bw.newLine();
                } else {
                    removedCount++;
                }
            }
            bw.flush();
        }
        // Replace the old bucket file with the updated one, only if changes have been made.
        if (removedCount > 0) {
            boolean deleted = bucketFile.delete();
            boolean renamed = tempFile.renameTo(bucketFile);
        } else {
            tempFile.delete(); // If no records were removed, discard the temporary file.
        }
        return removedCount;
    }

    private Record parseRecord(String line) {
        String[] parts = line.split(",");
        int index = Integer.parseInt(parts[0]);
        int year = Integer.parseInt(parts[1]);
        return new Record(index, year);
    }
}