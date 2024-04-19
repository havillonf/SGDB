import java.util.ArrayList;
import java.util.List;

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