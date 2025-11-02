import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

class CompressionPerformanceEvaluator {

    private static final int MIN_ARRAY_SIZE = 256;
    private static final int MAX_ARRAY_SIZE = 8192;
    private static final int ARRAY_COUNT = 300;
    private static final Random RANDOM = new Random(42);

    private static final double DEFAULT_LATENCY_MS = 10.0;
    private static final double DEFAULT_BANDWIDTH_MEGABITS_PER_SEC = 100.0;
    private static final int DEFAULT_MESSAGE_SIZE = 4096;
    private static final int DEFAULT_MAX_BITS_PER_VALUE = 24;
    private static final double DEFAULT_SPIKE_RATIO = 0.0;

    private CompressionPerformanceEvaluator() {
        throw new AssertionError("Utility class");
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        double latencyMs = DEFAULT_LATENCY_MS;
        double bandwidthMbps = DEFAULT_BANDWIDTH_MEGABITS_PER_SEC;
        int messageSize = DEFAULT_MESSAGE_SIZE;
        int maxBitsPerValue = DEFAULT_MAX_BITS_PER_VALUE;
        double spikeRatio = DEFAULT_SPIKE_RATIO;
        Integer spikeMinBits = null;

        for (int i = 0; i < args.length; i++) {
            String flag = args[i];
            if (!flag.startsWith("--")) {
                throw new IllegalArgumentException("Unexpected argument: " + flag);
            }
            if (i + 1 >= args.length) {
                throw new IllegalArgumentException("Missing value for " + flag);
            }
            String value = args[++i];
            switch (flag) {
                case "--latency":
                    latencyMs = Double.parseDouble(value);
                    break;
                case "--bandwidth":
                    bandwidthMbps = Double.parseDouble(value);
                    break;
                case "--messageSize":
                    messageSize = Integer.parseInt(value);
                    break;
                case "--maxBits":
                    maxBitsPerValue = Integer.parseInt(value);
                    break;
                case "--spikeRatio":
                    spikeRatio = Double.parseDouble(value);
                    break;
                case "--spikeMinBits":
                    spikeMinBits = Integer.parseInt(value);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }

        if (maxBitsPerValue < 1 || maxBitsPerValue > 31) {
            throw new IllegalArgumentException("maxBits must be between 1 and 31 inclusive.");
        }
        if (spikeRatio < 0.0 || spikeRatio > 1.0) {
            throw new IllegalArgumentException("spikeRatio must be between 0.0 and 1.0 inclusive.");
        }
        if (spikeMinBits == null) {
            spikeMinBits = Math.min(31, maxBitsPerValue + 4);
        }
        if (spikeMinBits < 1 || spikeMinBits > 31) {
            throw new IllegalArgumentException("spikeMinBits must be between 1 and 31 inclusive.");
        }
        if (spikeRatio > 0.0) {
            if (maxBitsPerValue >= 31) {
                throw new IllegalArgumentException("Cannot inject spike values when maxBits is 31.");
            }
            if (spikeMinBits <= maxBitsPerValue) {
                spikeMinBits = Math.min(31, maxBitsPerValue + 1);
            }
            if (spikeMinBits <= maxBitsPerValue) {
                throw new IllegalArgumentException("spikeMinBits must exceed maxBits when spikeRatio > 0.");
            }
        }
        if (maxBitsPerValue < 1 || maxBitsPerValue > 31) {
            throw new IllegalArgumentException("maxBitsPerValue must be between 1 and 31 inclusive.");
        }

        System.out.printf(
            "Configuration => Latency: %.2f ms, Bandwidth: %.2f Mbps, Message size: %d integers, Max bits/value: %d, Spike ratio: %.2f, Spike min bits: %d%n",
            latencyMs, bandwidthMbps, messageSize, maxBitsPerValue, spikeRatio, spikeMinBits
        );

        List<int[]> datasets = generateDatasets(maxBitsPerValue, spikeRatio, spikeMinBits);
        Map<String, BenchmarkResult> results = runBenchmarks(datasets);

        printBenchmarkSummary(results);
        evaluateNetworkScenarios(results, latencyMs, bandwidthMbps, messageSize);
    }

    private static List<int[]> generateDatasets(int maxBitsPerValue, double spikeRatio, int spikeMinBits) {
        List<int[]> datasets = new ArrayList<>(ARRAY_COUNT);
        int maxValue = computeMaxValueForBits(maxBitsPerValue);
        boolean injectSpikes = spikeRatio > 0.0;
        int spikeMinValue = 0;
        int spikeRange = 0;
        if (injectSpikes) {
            spikeMinValue = Math.max(computeMinValueForBits(spikeMinBits), safeIncrement(maxValue));
            long range = (long) Integer.MAX_VALUE - spikeMinValue + 1L;
            if (range <= 0) {
                throw new IllegalArgumentException("Spike minimum value exceeds Integer.MAX_VALUE.");
            }
            spikeRange = (int) range;
        }
        for (int i = 0; i < ARRAY_COUNT; i++) {
            int size = RANDOM.nextInt(MAX_ARRAY_SIZE - MIN_ARRAY_SIZE + 1) + MIN_ARRAY_SIZE;
            int[] data = new int[size];
            for (int j = 0; j < size; j++) {
                if (injectSpikes && RANDOM.nextDouble() < spikeRatio) {
                    data[j] = generateSpikeValue(spikeMinValue, spikeRange);
                } else {
                    data[j] = RANDOM.nextInt(maxValue) + 1; // strictly positive
                }
            }
            datasets.add(data);
        }
        return datasets;
    }

    private static Map<String, BenchmarkResult> runBenchmarks(List<int[]> datasets) {
        Map<String, BenchmarkResult> results = new LinkedHashMap<>();
        results.put("Overlapping", benchmark(datasets, () -> new CrossIntBitPacking()));
        results.put("Non-Overlapping", benchmark(datasets, () -> new NonCrossIntBitPacking()));
        results.put("Overflow-Aware", benchmark(datasets, () -> new OverflowBitPacking()));
        return results;
    }

    private static BenchmarkResult benchmark(List<int[]> datasets, BitPackingFactory factory) {
        BenchmarkResult result = new BenchmarkResult();
        for (int[] original : datasets) {
            int[] data = original.clone();
            BitPacking packing = factory.create();

            long compressStart = System.nanoTime();
            packing.compress(data);
            long compressNs = System.nanoTime() - compressStart;

            long getStart = System.nanoTime();
            for (int i = 0; i < data.length; i++) {
                packing.get(i);
            }
            long getNs = System.nanoTime() - getStart;

            int[] output = new int[data.length];
            long decompressStart = System.nanoTime();
            packing.decompress(output);
            long decompressNs = System.nanoTime() - decompressStart;

            result.totalCompressNs += compressNs;
            result.totalGetNs += getNs;
            result.totalDecompressNs += decompressNs;
            result.totalElements += data.length;
            result.arrayCount++;
            result.totalCompressedBits += estimateCompressedBits(packing);
        }
        return result;
    }

    private static double estimateCompressedBits(BitPacking packing) {
        if (packing instanceof CrossIntBitPacking) {
            CrossIntBitPacking cross = (CrossIntBitPacking) packing;
            return cross.data.length * 32.0;
        }
        if (packing instanceof NonCrossIntBitPacking) {
            NonCrossIntBitPacking nonCross = (NonCrossIntBitPacking) packing;
            return nonCross.data.length * 32.0;
        }
        if (packing instanceof OverflowBitPacking) {
            OverflowBitPacking overflow = (OverflowBitPacking) packing;
            double bits = overflow.data.length * 32.0;
            if (overflow.dataOverflowArea != null) {
                bits += overflow.dataOverflowArea.length * 32.0;
            }
            return bits;
        }
        throw new IllegalArgumentException("Unsupported BitPacking implementation: " + packing.getClass().getSimpleName());
    }

    private static void printBenchmarkSummary(Map<String, BenchmarkResult> results) {
        System.out.println("=== Compression Benchmark Summary ===");
        for (Map.Entry<String, BenchmarkResult> entry : results.entrySet()) {
            String name = entry.getKey();
            BenchmarkResult res = entry.getValue();
            double avgCompressMs = res.totalCompressNs / 1_000_000.0 / res.arrayCount;
            double avgGetMs = res.totalGetNs / 1_000_000.0 / res.arrayCount;
            double avgDecompressMs = res.totalDecompressNs / 1_000_000.0 / res.arrayCount;
            double compressThroughput = computeThroughput(res.totalCompressNs, res.totalElements);
            double decompressThroughput = computeThroughput(res.totalDecompressNs, res.totalElements);
            double getThroughput = computeThroughput(res.totalGetNs, res.totalElements);
            double bitsPerElement = res.totalCompressedBits / res.totalElements;

            System.out.println("\n[" + name + "]");
            System.out.printf("Arrays processed        : %d%n", res.arrayCount);
            System.out.printf("Average compress time   : %.4f ms%n", avgCompressMs);
            System.out.printf("Average get time        : %.4f ms%n", avgGetMs);
            System.out.printf("Average decompress time : %.4f ms%n", avgDecompressMs);
            System.out.printf("Compress throughput     : %.2f million ints/s%n", compressThroughput);
            System.out.printf("Get throughput          : %.2f million ints/s%n", getThroughput);
            System.out.printf("Decompress throughput   : %.2f million ints/s%n", decompressThroughput);
            System.out.printf("Average bits per int    : %.2f bits%n", bitsPerElement);
        }
    }

    private static double computeThroughput(long totalNs, long elementCount) {
        if (totalNs == 0) {
            return Double.POSITIVE_INFINITY;
        }
        double seconds = totalNs / 1_000_000_000.0;
        return (elementCount / seconds) / 1_000_000.0;
    }

    private static void evaluateNetworkScenarios(Map<String, BenchmarkResult> results,
                                                 double latencyMs,
                                                 double bandwidthMbps,
                                                 int messageSize) {
        System.out.println("\n=== Network Transmission Analysis ===");
        double bandwidthBitsPerMs = bandwidthMbps * 1_000_000.0 / 1000.0;
        double uncompressedBits = messageSize * 32.0;
        double uncompressedTransferMs = latencyMs + uncompressedBits / bandwidthBitsPerMs;
        System.out.printf("Latency: %.2f ms, Bandwidth: %.2f Mbps, Message size: %d integers%n",
                          latencyMs, bandwidthMbps, messageSize);
        System.out.printf("Uncompressed transmission time: %.4f ms%n", uncompressedTransferMs);

        for (Map.Entry<String, BenchmarkResult> entry : results.entrySet()) {
            String name = entry.getKey();
            BenchmarkResult res = entry.getValue();

            double compressTimePerElementMs = res.totalCompressNs / 1_000_000.0 / res.totalElements;
            double decompressTimePerElementMs = res.totalDecompressNs / 1_000_000.0 / res.totalElements;
            double bitsPerElement = res.totalCompressedBits / res.totalElements;

            double totalCompressMs = compressTimePerElementMs * messageSize;
            double totalDecompressMs = decompressTimePerElementMs * messageSize;
            double compressedBits = bitsPerElement * messageSize;
            double transferMs = latencyMs + compressedBits / bandwidthBitsPerMs;
            double totalPipelineMs = totalCompressMs + transferMs + totalDecompressMs;

            boolean beneficial = totalPipelineMs < uncompressedTransferMs;
            System.out.printf("%n[%s]%n", name);
            System.out.printf("  Estimated compressed bits : %.0f bits%n", compressedBits);
            System.out.printf("  Total time (compress + transfer + decompress): %.4f ms%n", totalPipelineMs);
            System.out.printf("  Compression beneficial? %s (savings: %.4f ms)%n",
                              beneficial ? "Yes" : "No",
                              uncompressedTransferMs - totalPipelineMs);
        }
    }

    private interface BitPackingFactory {
        BitPacking create();
    }

    private static int computeMaxValueForBits(int bits) {
        if (bits >= 31) {
            return Integer.MAX_VALUE;
        }
        return (1 << bits) - 1;
    }

    private static int computeMinValueForBits(int bits) {
        if (bits <= 1) {
            return 1;
        }
        if (bits >= 31) {
            return 1 << 30;
        }
        return 1 << (bits - 1);
    }

    private static int safeIncrement(int value) {
        if (value == Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return value + 1;
    }

    private static int generateSpikeValue(int minValue, int range) {
        if (range <= 0) {
            return minValue;
        }
        return RANDOM.nextInt(range) + minValue;
    }

    private static final class BenchmarkResult {
        long totalCompressNs;
        long totalGetNs;
        long totalDecompressNs;
        long totalElements;
        long arrayCount;
        double totalCompressedBits;
    }
}
