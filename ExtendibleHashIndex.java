import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

class ExtendibleHashIndex {
    List<HashBucket> directory;
    int globalDepth;

    public ExtendibleHashIndex(int depth) {
        this.globalDepth = depth;
        this.directory = new ArrayList<>(1 << depth); // Initialize with the correct size
        for (int i = 0; i < (1 << depth); i++) {
            this.directory.add(new HashBucket(depth));
        }
    }

    private int hash(int year) {
        return year & ((1 << globalDepth) - 1);
    }

    public void insert(Record record, PrintWriter output) {
        int index = hash(record.year);
        HashBucket bucket = directory.get(index);

        if (bucket.isFull()) {
            output.println("DUP DIR:/<" + globalDepth + ">,<" + bucket.localDepth + ">");
        } else {
            bucket.insertRecord(record);
            output.println("INC:" + record.year + "/" + globalDepth + "," + bucket.localDepth);
        }
    }

    public void search(int year, PrintWriter output) {
        int index = hash(year);
        HashBucket bucket = directory.get(index);
        int count = 0;
        for (Record record : bucket.records) {
            if (record.year == year) {
                count++;
            }
        }
        output.println("BUS:" + year + "/" + count);
    }

    public void delete(int year, PrintWriter output) {
        int index = hash(year);
        HashBucket bucket = directory.get(index);
        int initialSize = bucket.records.size();
        bucket.records.removeIf(record -> record.year == year);
        int removed = initialSize - bucket.records.size();
        output.println("REM:" + year + "/" + removed + "," + globalDepth + "," + bucket.localDepth);
    }
}