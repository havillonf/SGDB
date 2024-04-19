import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        File inputFile = new File("in.txt");
        Scanner scanner = new Scanner(inputFile);
        PrintWriter output = new PrintWriter("out.txt");

        // Read initial global depth from the first line
        String firstLine = scanner.nextLine();
        int globalDepth = Integer.parseInt(firstLine.split("/")[1]);
        ExtendibleHashIndex ehi = new ExtendibleHashIndex(globalDepth); // Use the depth from the file

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
        output.println("P:/" + ehi.globalDepth);
        output.close();
        scanner.close();
    }

    private static void loadComprasCsv(ExtendibleHashIndex ehi, String fileName) throws FileNotFoundException {
        Scanner csvScanner = new Scanner(new File(fileName));
        while (csvScanner.hasNextLine()) {
            String line = csvScanner.nextLine();
            String[] parts = line.split(",");
            int year = Integer.parseInt(parts[2].trim()); // Year is the third column
            double value = Double.parseDouble(parts[1].trim()); // Value is the second column
            ehi.insert(new Record(year, value), new PrintWriter(System.out)); // Output to System.out for initial load
        }
        csvScanner.close();
    }
}
