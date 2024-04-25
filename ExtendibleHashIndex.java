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
        int index = hash(record.year);
        System.out.println("Inserting " + record.year + " with index " + index);
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

    public void splitBucket(int index) throws IOException {
        HashBucket oldBucket = directory.get(index);

        if (oldBucket.isFull()) {
            // Atualizar o diretório
            int currentSize = directory.size();
            for (int i = 0; i < currentSize; i++) {
                directory.add(directory.get(i));
            }
            globalDepth++;

            // Criar um novo bucket com profundidade local aumentada
            int newBucketNumber = index + (int) Math.pow(2, globalDepth - 1);
            HashBucket newBucket = new HashBucket(oldBucket.localDepth + 1, newBucketNumber);
            directory.set(newBucketNumber, newBucket);

            // Rehash dos registros
            List<Record> tempRecords = HashBucket.readRecordsFromFile("buckets/" + oldBucket.bucketFile.getName());
            oldBucket.clear();
            newBucket.clear();

            for (Record record : tempRecords) {
                int newIndex = hash(record.year);
                if ((newIndex & ((1 << globalDepth) - 1)) == index) {
                    System.out.println("Reinserting year " + record.year + " on the old bucket");
                    oldBucket.insertRecord(record);
                } else {
                    System.out.println("Reinserting year " + record.year + " on the new bucket");
                    newBucket.insertRecord(record);
                }
                System.out.println();
            }

            // Atualizar a profundidade local dos buckets
            oldBucket.localDepth = oldBucket.localDepth + 1;
            newBucket.localDepth = oldBucket.localDepth;
        }
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
        if(HashBucket.readRecordsFromFile("buckets/" + bucket.bucketFile.getName()).isEmpty()){
            int bucketIndex = directory.indexOf(bucket);
            mergeBuckets(bucketIndex);
        }
    }
    
    public void mergeBuckets(int emptyBucketIndex) throws IOException {
        // Encontrar o índice do bucket irmão
        int mirrorIndex = emptyBucketIndex ^ (1 << (directory.get(emptyBucketIndex).localDepth - 1));
        HashBucket emptyBucket = directory.get(emptyBucketIndex);
        HashBucket mirrorBucket = directory.get(mirrorIndex);

        // Verificar se o merge é possível
        if (emptyBucket.localDepth == mirrorBucket.localDepth && !mirrorBucket.isFull()) {
            // Mover todos os registros do bucket irmão para o bucket vazio
            List<Record> mirrorRecords = HashBucket.readRecordsFromFile("buckets/" + mirrorBucket.bucketFile.getName());
            for (Record record : mirrorRecords) {
                emptyBucket.insertRecord(record);
            }
            mirrorBucket.clear();

            // Atualizar o diretório para apontar para o bucket combinado
            int combinedLocalDepth = emptyBucket.localDepth - 1;
            for (int i = 0; i < directory.size(); i++) {
                if ((i & ((1 << combinedLocalDepth) - 1)) == (emptyBucketIndex & ((1 << combinedLocalDepth) - 1))) {
                    directory.set(i, emptyBucket);
                }
            }

            // Atualizar a profundidade local dos buckets combinados
            emptyBucket.localDepth = combinedLocalDepth;

            // Verificar se a profundidade global pode ser reduzida
            boolean canReduceGlobalDepth = true;
            for (HashBucket bucket : directory) {
                if (bucket.localDepth == globalDepth) {
                    canReduceGlobalDepth = false;
                    break;
                }
            }
            if (canReduceGlobalDepth) {
                globalDepth--;
                // Reduzir o tamanho do diretório pela metade
                for (int i = 0; i < directory.size() / 2; i++) {
                    directory.remove(directory.size() - 1);
                }
            }
        }
    }


    public void printDirectory() throws FileNotFoundException {
        PrintWriter output = new PrintWriter("files/directory.txt");
        output.println("PG:" + globalDepth);
        for (int i = 0; i < directory.size(); i++) {
            String index = directory.get(i).bucketFile.getName().split("\\.")[0];
            output.println("Bucket: (" + i + ") Index: " + index + ", " + "PL: " + directory.get(i).localDepth);
        }
        output.close();
    }
}