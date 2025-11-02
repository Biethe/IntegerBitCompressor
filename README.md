# Integer Bit Compressor

Java tooling for experimenting with integer compression strategies that pack values at the bit level.  
The repository contains multiple compression implementations, a factory that selects the right strategy
based on the incoming data, benchmarking utilities, and a CLI demo for quick round-trips.

## Features
- Multiple `BitPacking` implementations: overlapping, non-overlapping, overflow-aware, and negative-aware.
- `Compressor` factory that inspects incoming arrays and chooses the appropriate encoder (with interactive guidance when needed).
- Rich statistics helper (`Statistics`) that computes mean/standard deviation/sparsity in a single pass.
- `CompressionPerformanceEvaluator` benchmark harness for profiling throughput, compression ratios, and network trade-offs.
- CLI demo (`Main`) that accepts data via stdin or file, runs compression/decompression, and reports timings.

## Getting Started

### Prerequisites
- Java 11 or higher (the code uses standard library features only).

### Compilation
```bash
javac *.java
```

This produces `.class` files for all sources in the repository.

## Running the Programs

### Compression Demo (`Main`)
Allows you to supply an integer array either from standard input or a file, compress it with the auto-selected strategy, and print the decompressed output with timing information.

```bash
# From stdin (terminate input with Ctrl+D on Unix/macOS or Ctrl+Z then Enter on Windows)
java Main
1 2 3 4 5

# From a file (values can be separated by whitespace or commas)
java Main path/to/values.txt
```

The output prints the decompressed array and the time spent compressing/decompressing in milliseconds.

### Benchmark Harness (`CompressionPerformanceEvaluator`)
Generates random positive arrays, runs every `BitPacking` strategy, and estimates performance as well as network-transmission savings.

```bash
java CompressionPerformanceEvaluator \
  --latency 10 \
  --bandwidth 100 \
  --messageSize 4096 \
  --maxBits 16 \
  --spikeRatio 0.1 \
  --spikeMinBits 24
```

All flags are optional. Omitted values fall back to defaults:

| Flag | Meaning | Default |
|------|---------|---------|
| `--latency` | Network latency in ms | `10` |
| `--bandwidth` | Bandwidth in Mbps | `100` |
| `--messageSize` | Array length used in the transfer simulation | `4096` |
| `--maxBits` | Maximum bit width for most generated integers | `24` |
| `--spikeRatio` | Fraction of integers that exceed `--maxBits` | `0.0` |
| `--spikeMinBits` | Minimum bit width for spike values | `maxBits + 4` |

The benchmark summary lists average timings, throughputs, and bits per integer for each implementation, followed by the network analysis.

## Code Organization
- `BitPacking.java` — interface implemented by every compression strategy (`compress`, `get`, `decompress`).
- `CrossIntBitPacking.java` — overlapping bit packing that allows values to span integers.
- `NonCrossIntBitPacking.java` — compact packing when each value fits neatly inside 32-bit words.
- `OverflowBitPacking.java` — two-tier packing with an overflow area for outliers.
- `NegativeIntegerBitPacking.java` — handles negative values by reusing the overflow infrastructure.
- `Compressor.java` — static factory to select the correct `BitPacking` implementation; interacts with users when positive-only arrays are insufficiently sparse.
- `Statistics.java` — single-pass computation of mean, standard deviation, and sparsity with primitive-array fast paths.
- `Main.java` — CLI demo for compressing and verifying integer arrays.
- `CompressionPerformanceEvaluator.java` — benchmarking and network trade-off analyzer.

## Example Workflow
1. Collect or generate your integer dataset (positive or mixed).
2. Run `java Main data.txt` to confirm end-to-end compression/decompression.
3. Use `java CompressionPerformanceEvaluator` with appropriate flags to profile performance and decide whether compression pays off for your network constraints.

## Contributors
- Initial development: Bierhoff Theolien
- Recent enhancements, refactors, and tooling: Codex assistant collaborating with Bierhoff Theolien

Feel free to open issues or submit pull requests for additional compression strategies, optimizations, or documentation improvements.
