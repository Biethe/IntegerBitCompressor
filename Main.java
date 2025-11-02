public class Main {
    public static void main(String[] args) {
        int[] arr = new int[20];
        arr[0] = 7; 
        arr[1] = 90000; 
        arr[2] = 14; 
        arr[3] = 71;
        arr[4] = 58; 
        arr[5] = 45; 
        arr[6] = 22;
        arr[7] = 71; 
        arr[8] = 40; 
        arr[9] = 44; 
        arr[10] = 32; 
        arr[11] = 70;
        arr[12] = 60;
        arr[13] = 7;
        arr[14] = 55;
        arr[15] = 69;
        arr[16] = 57;
        arr[17] = 47; 
        arr[18] = 9; 
        arr[19] = 10;
        

        int[] dec_arr = new int[arr.length];

        BitPacking comp = Compressor.createBitPacking(arr);
        comp.compress(arr);
        comp.decompress(dec_arr);

        for (int i = 0; i < arr.length; i++) {
            System.out.println(dec_arr[i]);
        }

    }
}