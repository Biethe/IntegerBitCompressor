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

        int[] dec_arr = new int[20];

        CrossIntBitPacking bitpacker = new CrossIntBitPacking();

        NonCrossIntBitPacking bitpacker1 = new NonCrossIntBitPacking();

        bitpacker.compress(arr);
        bitpacker.decompress(dec_arr);

        for(int i=0; i<20; i++){
            System.out.print(arr[i] + " ");
        }

        System.out.println("Original array");

        for(int i=0; i<20; i++){
            System.out.print(dec_arr[i] + " ");
        }

        System.out.println("Decompressed array with overlapping integers");

        bitpacker1.compress(arr);
        bitpacker1.decompress(dec_arr);

        for(int i=0; i<20; i++){
            System.out.print(dec_arr[i] + " ");
        }

        System.out.println("Decompressed array with non-overlapping integers");


                


    }
}