import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

class ExtendibleHashIndex {
    List<HashBucket> directory;
    int globalDepth;

    public ExtendibleHashIndex(int depth) {
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
        int index = hash(record.year);
        HashBucket bucket = directory.get(index);

        if (bucket.isFull()) {
            output.println("DUP DIR:/<" + globalDepth + ">,<" + bucket.localDepth + ">");
        } else {
            bucket.insertRecord(record);
            output.println("INC:" + record.year + "/" + globalDepth + "," + bucket.localDepth);
        }
    }

    public void search(int year, PrintWriter output) throws IOException {
        int index = hash(year);
        System.out.println("Resultado da função hash: " + index);
        HashBucket bucket = directory.get(index);
        int count = bucket.search(year);
        output.println("BUS:" + year + "/" + count);
    }

    public void delete(int year, PrintWriter output) throws IOException {
        int index = hash(year);
        HashBucket bucket = directory.get(index);
        int removed = bucket.delete(year);
        output.println("REM:" + year + "/" + removed + "," + globalDepth + "," + bucket.localDepth);
    }
}