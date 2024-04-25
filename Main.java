import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        File inputFile = new File("files/in.txt");
        Scanner scanner = new Scanner(inputFile);
        PrintWriter output = new PrintWriter("files/out.txt");

        // Read initial global depth from the first line
        String firstLine = scanner.nextLine();
        int globalDepth = Integer.parseInt(firstLine.split("/")[1]);
        ExtendibleHashIndex ehi = new ExtendibleHashIndex(globalDepth);

        output.println(firstLine); // Echo the first line to out.txt

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] parts = line.split(":");
            String command = parts[0];
            int year = Integer.parseInt(parts[1]);

            switch (command) {
                case "INC":
                    List<Integer> indexes = getIndexesFromYear(year);
                    for (Integer index : indexes) {
                        ehi.insert(new Record(index, year), output);
                    }
                    break;
                case "REM":
                    ehi.delete(year, output);
                    break;
                case "BUS=":
                    ehi.search(year, output);
                    break;
            }
        }
        output.println("P:" + ehi.globalDepth);
        ehi.printDirectory();
        output.close();
        scanner.close();
    }

    private static void createBuckets(int globalDepth) {

    }

    private static List<Integer> getIndexesFromYear(int inputYear){
        List<Integer> indexes = new ArrayList<>();
        //Reader is automatically closed by Try-With-Resources
        try (BufferedReader br = new BufferedReader(new FileReader("files/compras.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                int index = Integer.parseInt(parts[0].trim());
                int year = Integer.parseInt(parts[2].trim());
                if(year == inputYear){
                    indexes.add(index);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return indexes;
    }

    public static String lastBits(int n, int depth){
        int lastDigits = n & (1 << depth) - 1;

        return Integer.toBinaryString(lastDigits);
    }
}
