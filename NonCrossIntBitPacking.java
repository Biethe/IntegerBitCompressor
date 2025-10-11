public class NonCrossIntBitPacking {
    private final int[] data;
    private final int nOfBitsPerValue;

    public NonCrossIntBitPacking(int nOfValues, int bitsPerValue){

        nOfBitsPerValue = bitsPerValue;
        // Compute the number of integers(size of data) necessary for the compression by doing a ceiling boundary
        int nOfIntegersPerInt = (32/nOfBitsPerValue);
        int nOfRequiredIntegers = (nOfValues + (nOfIntegersPerInt-1))/nOfIntegersPerInt;
        data = new int[nOfRequiredIntegers];
    
        //(Need to be checked/refined) Create mask then put all bits of data to 0
        int mask = (((1 << 31) - 1) << 1)^1;
        for (int i = 0; i<nOfRequiredIntegers; i++){
            data[i] &= ~mask; //set all of integers in data to zero(32 zeros in binary format)
        }

    }

    public void compress(int index, int value){

        int nOfvaluesPerInt = 32/nOfBitsPerValue;
        int intIndex = index/nOfvaluesPerInt;
        int bitIndex = (index % nOfvaluesPerInt);

        data[intIndex] |= (value << (bitIndex*nOfBitsPerValue));
    }

    public int decompress(int index) {

        int nOfvaluesPerInt = 32/nOfBitsPerValue;
        int intIndex = index/nOfvaluesPerInt;
        int bitIndex = (index % nOfvaluesPerInt);

        int value = (data[intIndex] & ((1 << nOfBitsPerValue)-1)<<(bitIndex*nOfBitsPerValue)) >> (bitIndex*nOfBitsPerValue);
        //In case we have leading ones which might happen a few times within the array.
        value &= ((1 << nOfBitsPerValue) - 1);
        return value;
    }
}