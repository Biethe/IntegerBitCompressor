import java.util.Arrays;

public class NonCrossIntBitPacking implements BitPacking{
    protected int[] data;
    protected int nOfBitsPerValue;


    @Override
    public void compress(int[] arr){

        int max = Arrays.stream(arr).max().getAsInt();
        nOfBitsPerValue = 32 - Integer.numberOfLeadingZeros(max);

        int nOfIntegersPerInt = 32 / nOfBitsPerValue;

        int nOfRequiredIntegers = (arr.length + (nOfIntegersPerInt-1))/nOfIntegersPerInt;
        data = new int[nOfRequiredIntegers];
    
        //(Need to be checked/refined) Create mask then put all bits of data to 0
        int mask = (((1 << 31) - 1) << 1)^1;
        for (int i = 0; i<nOfRequiredIntegers; i++){
            data[i] &= ~mask; //set all of integers in data to zero(32 zeros in binary format)
        }

        for(int i=0; i<arr.length; i++){
            int nOfvaluesPerInt = 32/nOfBitsPerValue;
            int intIndex = i/nOfvaluesPerInt;
            int bitIndex = (i % nOfvaluesPerInt);

            data[intIndex] |= (arr[i] << (bitIndex*nOfBitsPerValue));
        }
    }

    @Override
    public int get(int index) {

        int nOfvaluesPerInt = 32/nOfBitsPerValue;
        int intIndex = index/nOfvaluesPerInt;
        int bitIndex = (index % nOfvaluesPerInt);

        int value = (data[intIndex] & ((1 << nOfBitsPerValue)-1)<<(bitIndex*nOfBitsPerValue)) >> (bitIndex*nOfBitsPerValue);
        //In case we have leading ones which might happen a few times within the array.
        value &= ((1 << nOfBitsPerValue) - 1);
        return value;
    }

    @Override
    public void decompress(int[] arr){
        for(int i=0; i<arr.length; i++){
            arr[i] = get(i);
        }
    }
}