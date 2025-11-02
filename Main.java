import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        try {
            int[] input = readInput(args);
            System.out.println("=== Compression Demo ===");
            System.out.println("Input array size: " + input.length);

            BitPacking bitPacking = Compressor.createBitPacking(input);
            int[] decompressed = new int[input.length];

            long compressStart = System.nanoTime();
            bitPacking.compress(input);
            long compressNs = System.nanoTime() - compressStart;

            long decompressStart = System.nanoTime();
            bitPacking.decompress(decompressed);
            long decompressNs = System.nanoTime() - decompressStart;

            System.out.println();
            System.out.println("--- Decompressed Array ---");
            System.out.println(Arrays.toString(decompressed));
            System.out.println();
            System.out.println("--- Timing ---");
            System.out.printf("Compression time : %.4f ms%n", compressNs / 1_000_000.0);
            System.out.printf("Decompression time: %.4f ms%n", decompressNs / 1_000_000.0);
        } catch (Exception ex) {
            System.err.println("Compression pipeline failed: " + ex.getMessage());
            ex.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static int[] readInput(String[] args) throws IOException {
        if (args.length > 0) {
            return readFromFile(args[0]);
        }
        return readFromStdIn();
    }

    private static int[] readFromFile(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            List<Integer> values = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                parseLine(line, values);
            }
            ensureNotEmpty(values);
            return toArray(values);
        }
    }

    private static int[] readFromStdIn() throws IOException {
        System.out.println("Enter integers separated by spaces or commas. Press Ctrl+D (Unix) / Ctrl+Z (Windows) to finish.");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            List<Integer> values = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                parseLine(line, values);
            }
            ensureNotEmpty(values);
            return toArray(values);
        }
    }

    private static void parseLine(String line, List<Integer> values) {
        String[] tokens = line.trim().split("[,\\s]+");
        for (String token : tokens) {
            if (!token.isEmpty()) {
                values.add(Integer.parseInt(token));
            }
        }
    }

    private static void ensureNotEmpty(List<Integer> values) {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("No integers provided.");
        }
    }

    private static int[] toArray(List<Integer> values) {
        int[] result = new int[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }
        return result;
    }
}
