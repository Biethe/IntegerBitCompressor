import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        int[] arr = new int[20];
        arr[0] = 7;
        arr[1] = 9;
        arr[2] = 14;
        arr[3] = 71;
        arr[4] = 25;
        arr[5] = 45;
        arr[6] = 5;
        arr[7] = 221;
        arr[8] = 55;
        arr[9] = 255;
        arr[10] = 352;
        arr[11] = 589;
        arr[12] = 1;
        arr[13] = 0;
        arr[14] = 55;
        arr[15] = 87;
        arr[16] = 57;
        arr[17] = 47;
        arr[18] = 98;
        arr[19] = 100;
                
        int max = Arrays.stream(arr).max().getAsInt();
        int nOfBitsPerValue = 32 - Integer.numberOfLeadingZeros(max);
        int nOfValue = arr.length;

        

        for (int i=0; i<nOfValue; i++){
            System.out.print(arr[i] + " ");
        }

        System.out.println("Original array");

        
        CrossIntBitPacking bitpacker_1 = new CrossIntBitPacking(nOfValue, nOfBitsPerValue);

        for (int i=0; i<nOfValue; i++){
            bitpacker_1.compress(i, arr[i]);
            System.out.print(bitpacker_1.decompress(i) + " ");
        }
        System.out.println("CrossingBitPacking check");

        
        NonCrossIntBitPacking bitpacker_2 = new NonCrossIntBitPacking(nOfValue, nOfBitsPerValue);

        for (int i=0; i<nOfValue; i++){
            bitpacker_2.compress(i, arr[i]);
            System.out.print(bitpacker_2.decompress(i) + " ");
        }
        System.out.println("NonCrossingBitPacking check");

    }
}