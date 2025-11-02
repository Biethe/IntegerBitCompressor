import java.util.Scanner;

class Compressor {

    private static final Scanner INPUT = new Scanner(System.in);

    private Compressor() {
        throw new AssertionError("Utility class should not be instantiated.");
    }

    public static BitPacking createBitPacking(int[] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Data array must contain at least one value.");
        }

        boolean hasNegative = containsNegativeValues(data);
        if (hasNegative) {
            System.out.println("Your data contains at least a negative integer.");
            System.out.println("Therefore we are going to use a compression type that is best suited for your data.");
            return new NegativeIntegerBitPacking();
        }

        double sparsity = computeSparsity(data);

        if (!hasNegative && sparsity < 0.3) {
            return promptForPositiveCompressionChoice(sparsity);
        }

        if (!hasNegative && sparsity >= 0.3) {
            System.out.println("Your just passed an array with non-negative integers.");
            System.out.println("Since the data is sparsed, we are going to create an overflow space to compress the largest integers.");
            return new OverflowBitPacking();
        }

        return new NonCrossIntBitPacking();
    }

    private static boolean containsNegativeValues(int[] data) {
        for (int value : data) {
            if (value < 0) {
                return true;
            }
        }
        return false;
    }

    private static double computeSparsity(int[] data) {
        int[] bitArr = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            bitArr[i] = 32-Integer.numberOfLeadingZeros(data[i]);
            if(Integer.numberOfLeadingZeros(data[i]) == 32){
                bitArr[i] = 1;
            }
        }
        Statistics stats = new Statistics();
        return stats.computeSparsity(bitArr);
    }

    private static BitPacking promptForPositiveCompressionChoice(double sparsity) {
        System.out.println(
            "Your data contains only positive values and isn't sparse enough (sparsity=" + sparsity
                + ") to build an overflow area."
        );
        System.out.println(
            "You can compress with overlapping integers by typing 0, or opt for non-overlapping compression by typing 1."
        );

        while (true) {
            System.out.print("Enter 0 or 1: ");
            String input = INPUT.nextLine().trim();
            if ("0".equals(input)) {
                System.out.println("You chose the overlapping compression type");

                return new CrossIntBitPacking();
            }
            if ("1".equals(input)) {
                System.out.println("You chose the non-overlapping compression type");
                return new NonCrossIntBitPacking();
            }
            System.out.println("Invalid choice. Please enter 0 for overlapping or 1 for non-overlapping compression.");
        }
    }
}
