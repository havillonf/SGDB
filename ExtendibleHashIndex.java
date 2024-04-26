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
        if(bucket.isEmpty()){
            int bucketIndex = directory.indexOf(bucket);
            HashBucket finalBucket = mergeBuckets(bucketIndex);
            System.out.println("finalBucket: " + finalBucket.bucketFile.getName());
            output.println("REM:" + year + "/" + removed + "," + globalDepth + "," + finalBucket.localDepth);
        }else{
            output.println("REM:" + year + "/" + removed + "," + globalDepth + "," + bucket.localDepth);
        }
    }
    
    public HashBucket mergeBuckets(int index) throws IOException {
        System.out.println("Entrou na função MergeBuckets o indice:" + index);
        if(globalDepth == 2){
            System.out.println(directory.get(index).bucketFile.getName());
            return directory.get(index);
        }
        // Encontrar o índice do bucket espelho
        HashBucket bucket = directory.get(index);

        int siblingIndex = index ^ (1 << (bucket.localDepth - 1));
        System.out.println("siblingIndex:" + siblingIndex);
        boolean isSiblingLowerIndex = siblingIndex < index;

        HashBucket lowerIndexBucket = isSiblingLowerIndex ? directory.get(siblingIndex) : directory.get(index);
        HashBucket higherIndexBucket = isSiblingLowerIndex ? directory.get(index) : directory.get(siblingIndex);

        System.out.println("lower: " + lowerIndexBucket.bucketFile.getName());
        System.out.println("higher: " + higherIndexBucket.bucketFile.getName());

        if(lowerIndexBucket.isEmpty()){
            System.out.println("lower ficou vazio");
            moveRecords(lowerIndexBucket, higherIndexBucket);
            merge(higherIndexBucket, lowerIndexBucket);
        }else if(higherIndexBucket.isEmpty()){
            System.out.println("higher ficou vazio");
            merge(higherIndexBucket, lowerIndexBucket);
        }else{
            return lowerIndexBucket;
        }
        if(isSiblingLowerIndex){
            mergeBuckets(siblingIndex);
        }else{
            mergeBuckets(index);
        }
        return bucket;
    }

    public void moveRecords(HashBucket emptyBucket, HashBucket bucket) throws IOException {
        System.out.println("Moveu os registros de ("+bucket.bucketFile.getName()+")" + " para ("+emptyBucket.bucketFile.getName()+")");
        List<Record> siblingRecords = HashBucket.readRecordsFromFile("buckets/" + bucket.bucketFile.getName());
        for (Record record : siblingRecords) {
            System.out.println("Registro: " + record.year);
            emptyBucket.insertRecord(record);
        }
        bucket.clear();
    }

    public void merge(HashBucket emptyBucket, HashBucket bucket) throws IOException {
        System.out.println("Entrou pra fazer o merge");
        System.out.println("Empty bucket: " + emptyBucket.bucketFile.getName() + " index: " + directory.indexOf(emptyBucket));
        System.out.println("Bucket: " + bucket.bucketFile.getName() + " index: " + directory.indexOf(bucket));

        int lowerIndexBucketNumber = Math.min(directory.indexOf(emptyBucket), directory.indexOf(bucket));
        int higherIndexBucketNumber = Math.max(directory.indexOf(emptyBucket), directory.indexOf(bucket));

        // Excluir o arquivo do bucket irmão
        File higherIndexFile = new File("buckets/" + directory.get(higherIndexBucketNumber).bucketFile.getName());
        System.out.println("Apagando o arquivo: " + directory.get(higherIndexBucketNumber).bucketFile.getName());
        if (higherIndexFile.exists()) {
            higherIndexFile.delete();
        }

        // Atualizar o diretório para apontar para o bucket de menor índice
        int combinedLocalDepth = emptyBucket.localDepth - 1;
        int mask = (1 << combinedLocalDepth) - 1;

        for (int i = 0; i < directory.size(); i++) {
            if ((i & mask) == (lowerIndexBucketNumber & mask)) {
                directory.set(i, directory.get(lowerIndexBucketNumber));
            }
        }

        System.out.println();
        for(int i = 0; i < directory.size(); i++) {
            if(directory.get(i) != null){
                System.out.println("directory at: " + i + " - " + directory.get(i).bucketFile.getName());
            }
        }
        System.out.println();

        // Atualizar a profundidade local do bucket original
        emptyBucket.localDepth = combinedLocalDepth;
        bucket.localDepth = combinedLocalDepth;
        System.out.println("profundidade nova: " + combinedLocalDepth);



//        // Remover o bucket irmão da memória
//        directory.set(directory.indexOf(emptyBucket), null);

        // Verificar se a profundidade global pode ser reduzida
        boolean canReduceGlobalDepth = true;
        for (HashBucket b : directory) {
            if (b != null && b.localDepth == globalDepth) {
                canReduceGlobalDepth = false;
                break;
            }
        }
        if (canReduceGlobalDepth) {
            System.out.println("Reduzindo a profundidade global");
            reduceGlobalDepth();
        }
    }

    public void reduceGlobalDepth(){
        globalDepth--;
        // Reduzir o tamanho do diretório pela metade
        int size = directory.size();
        for (int i = 0; i < (size / 2); i++) {
            directory.removeLast();
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