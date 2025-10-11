public class CrossIntBitPacking {
    final private int[] data;
    final private int nOfBitsPerValue;
    

    public CrossIntBitPacking(int nOfValues, int bitsPerValue) {

        nOfBitsPerValue = bitsPerValue;
        // Compute the number of integers(size of data) necessary for the compression by doing a ceiling boundary
        int nOfRequiredIntegers = (nOfBitsPerValue*nOfValues+31)/32;
        data = new int[nOfRequiredIntegers];

        //(Need to be checked/refined) Create mask then put all bits of data to 0
        int mask = (((1 << 31) - 1) << 1)^1;
        for (int i = 0; i<nOfRequiredIntegers; i++){
            data[i] &= ~mask;
        }

    }

    public void compress(int index, int value){

        int bitIndex = nOfBitsPerValue * index;
        int intIndex = bitIndex/32;
        int bitOffset = bitIndex%32;

        data[intIndex] |= (value << bitOffset);

        // Handle cross-integer bitpacking

        if (nOfBitsPerValue > 32 - bitOffset){
            data[intIndex + 1] |= (value >> (32 - bitOffset));
        }


    }

    // Gives negative numbers on some examples, to be corrected
    int decompress(int index) {

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

}