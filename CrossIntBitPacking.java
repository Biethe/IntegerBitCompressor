import java.util.Arrays;

public class CrossIntBitPacking {
    private int[] data;
    private int nOfBitsPerValue;

    public void compress(int[] arr){

        int max = Arrays.stream(arr).max().getAsInt();
        nOfBitsPerValue = 32 - Integer.numberOfLeadingZeros(max);
        // Compute the number of integers(size of data) necessary for the compression by doing a ceiling boundary
        int nOfRequiredIntegers = (nOfBitsPerValue*arr.length+31)/32;
        data = new int[nOfRequiredIntegers];

        //(Need to be checked/refined) Create mask then put all bits of data to 0
        int mask = (((1 << 31) - 1) << 1)^1;
        for (int i = 0; i<nOfRequiredIntegers; i++){
            data[i] &= ~mask;
        }

        for(int i=0; i<arr.length; i++){
            int bitIndex = nOfBitsPerValue * i;
            int intIndex = bitIndex/32;
            int bitOffset = bitIndex%32;

            data[intIndex] |= (arr[i] << bitOffset);

            // Handle cross-integer bitpacking

            if (nOfBitsPerValue > 32 - bitOffset){
                data[intIndex + 1] |= (arr[i] >> (32 - bitOffset));
            }
        }

    }

    
    public int get(int index) {

        int bitIndex = nOfBitsPerValue * index;
        int byteIndex = bitIndex/32;
        int bitOffset = bitIndex%32;

        int value = (data[byteIndex] & (((1 << nOfBitsPerValue)-1) << bitOffset))>> bitOffset;     
        
        
        // Handle cross-integer bitpacked integers
        if (nOfBitsPerValue > 32 - bitOffset){
            int mask = (1 << (bitOffset + nOfBitsPerValue - 32))-1;
            value &= ((1 << (32-bitOffset)) - 1); //In case value has leading ones caused by the operation >> bitOffet
            value |= ((data[byteIndex+1] & mask) << (32- bitOffset));
        }
        
        return value;
    }

    public void decompress(int[] arr){
        for(int i=0; i<arr.length; i++){
            arr[i] = get(i);
        }        
    }



}