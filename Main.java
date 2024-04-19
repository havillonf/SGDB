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
        ExtendibleHashIndex ehi = new ExtendibleHashIndex(globalDepth);

        // Load data from CSV file before processing commands
        loadComprasCsv(ehi, "compras.csv");

        output.println(firstLine); // Echo the first line to out.txt

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] parts = line.split(":");
            String command = parts[0];
            int key = Integer.parseInt(parts[1]);

            switch (command) {
                case "INC":
                    ehi.insert(new Record(key, 0.0), output);
                    break;
                case "REM":
                    ehi.delete(key, output);
                    break;
                case "BUS":
                    ehi.search(key, output);
                    break;
            }
        }

        output.close();
        scanner.close();
    }

    private static void loadComprasCsv(ExtendibleHashIndex ehi, String fileName) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                int year = Integer.parseInt(parts[2].trim());
                double value = Double.parseDouble(parts[1].trim());
                PrintWriter dummyOutput = new PrintWriter(System.out); // Dummy output for loading
                ehi.insert(new Record(year, value), dummyOutput);
                dummyOutput.close();
            }
        }
    }
}
