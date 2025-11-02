import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

final class SampleDataGenerator {

    static final int DEFAULT_SAMPLE_SIZE = 16;

    private SampleDataGenerator() {
        throw new AssertionError("Utility class should not be instantiated.");
    }

    static void generateSampleFiles(Path directory) throws IOException {
        generateSampleFiles(directory, DEFAULT_SAMPLE_SIZE);
    }

    static void generateSampleFiles(Path directory, int sampleSize) throws IOException {
        if (sampleSize <= 0) {
            throw new IllegalArgumentException("Sample size must be a positive integer.");
        }

        Files.createDirectories(directory);

        Map<String, int[]> samples = createSamples(sampleSize);

        for (Map.Entry<String, int[]> entry : samples.entrySet()) {
            Path output = directory.resolve(entry.getKey() + ".txt");
            String content = toLine(entry.getValue()) + System.lineSeparator();
            Files.writeString(
                output,
                content,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
            );
        }
    }

    private static Map<String, int[]> createSamples(int sampleSize) {
        Map<String, int[]> samples = new LinkedHashMap<>();

        samples.put(
            "negative_sample",
            resizePattern(new int[]{-3, 42, 17, -512, 2048, -1, 0, 15}, sampleSize)
        );

        samples.put(
            "overflow_sample",
            resizePattern(new int[]{32768, 1, 1, 65535, 2, 1, 131072, 3, 1, 1, 262144, 5}, sampleSize)
        );

        samples.put(
            "cross_sample",
            resizePattern(new int[]{31, 7, 18, 9, 12, 6, 22, 15, 3, 28, 4, 11}, sampleSize)
        );

        samples.put(
            "non_cross_sample",
            resizePattern(new int[]{12, 200, 45, 130, 255, 64, 19, 88, 170, 5, 240, 33}, sampleSize)
        );

        return samples;
    }

    private static String toLine(int[] values) {
        return Arrays.stream(values)
            .mapToObj(String::valueOf)
            .collect(Collectors.joining(" "));
    }

    private static int[] resizePattern(int[] pattern, int sampleSize) {
        int[] result = new int[sampleSize];
        for (int i = 0; i < sampleSize; i++) {
            result[i] = pattern[i % pattern.length];
        }
        return result;
    }
}
