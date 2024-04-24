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
            int year = Integer.parseInt(parts[1]);

            switch (command) {
                case "INC":
                    ehi.insert(new Record(0, year), output);
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

        output.close();
        scanner.close();
    }

    private static void loadComprasCsv(ExtendibleHashIndex ehi, String fileName) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                int index = Integer.parseInt(parts[0].trim());
                int year = Integer.parseInt(parts[2].trim());
                PrintWriter unusedOutput = new PrintWriter(System.out); // Dummy output for loading
                ehi.insert(new Record(index, year), unusedOutput);
                unusedOutput.close();
            }
        }
    }

    public static String lastBits(int n, int depth){
        int lastDigits = n & (1 << depth) - 1; // Aplica a máscara ao número

        StringBuilder binary = new StringBuilder(Integer.toBinaryString(lastDigits));

        while (binary.length() < depth) {
            binary.insert(0, "0");
        }
        System.out.println("Últimos " + depth + " dígitos binários: " + binary);

        return binary.toString();
    }
}
