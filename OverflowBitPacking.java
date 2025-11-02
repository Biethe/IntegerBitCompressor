import java.util.Arrays;

class OverflowBitPacking implements BitPacking{
    protected int[] dataOverflowArea;
    protected int[] data;
    protected int nOfBitsPerValueOverflow;
    protected int nOfBitsPerValue;
    
    

    public int[][] arraySplitter(int[] arr){
        int[] overflowArea;
        int[] normalArea;
        int sizeOfOverflowArea = 0;
        int[] bitArr = new int[arr.length];
        double meanOfBitsPerInt;
        double stdOfBitsPerInt;

        for(int i= 0; i < arr.length; i++){
            bitArr[i] = 32 - Integer.numberOfLeadingZeros(arr[i]);
            if(arr[i] == 0){
                bitArr[i] = 1;
            }
        }

        Statistics stats = new Statistics();
        meanOfBitsPerInt = stats.computeMean(bitArr);

        stdOfBitsPerInt = (int) stats.computeStd(bitArr);
        double sparsity = stats.computeSparsity(bitArr);
        
        
        for(int i= 0; i < arr.length; i++){
            if ((bitArr[i] - meanOfBitsPerInt) >= (sparsity*stdOfBitsPerInt)){
                sizeOfOverflowArea++;
            }
        }

        normalArea = arr.clone();
        overflowArea = new int[sizeOfOverflowArea];

        int a = 0; //count the number of time we have met a value in the overflow Area
        for(int i= 0; i < arr.length; i++){
            if ((bitArr[i]- meanOfBitsPerInt) >= (sparsity*stdOfBitsPerInt)){
                overflowArea[a] = arr[i];
                normalArea[i] = a;
                a++;
            }
        }
        return new int[][] {normalArea, overflowArea};
    }

    @Override
    public void compress(int[] arr){
        int[][] splitArray = arraySplitter(arr);
        int[] normalArea  = splitArray[0];
        int[] overflowArea = splitArray[1];

        //normal area compression
        int max = Arrays.stream(normalArea).max().getAsInt();
        nOfBitsPerValue = 32 - Integer.numberOfLeadingZeros(max)+1;
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

            int encodedValue = normalArea[i];
            if(normalArea[i] != arr[i]){
                encodedValue |= 1 << (nOfBitsPerValue-1);
            }

            data[intIndex] |= (encodedValue << bitOffset);

            if (nOfBitsPerValue > (32 - bitOffset) && intIndex + 1 < data.length){
                data[intIndex + 1] |= (encodedValue >>> (32 - bitOffset));
            }
    
        }

        //overflow area compression
        CrossIntBitPacking overflowBitPacker = new CrossIntBitPacking();
        overflowBitPacker.compress(overflowArea);
        dataOverflowArea = overflowBitPacker.data.clone();
        nOfBitsPerValueOverflow = overflowBitPacker.nOfBitsPerValue;
    }

    @Override
    public int get(int index){
        int bitIndex = nOfBitsPerValue * index;
        int intIndex = bitIndex/32;
        int bitOffset = bitIndex%32;

        long lower = data[intIndex] & 0xFFFFFFFFL;
        long combined = lower >>> bitOffset;

        if (nOfBitsPerValue > 32 - bitOffset && intIndex + 1 < data.length){
            int spillBits = nOfBitsPerValue - (32 - bitOffset);
            long upper = data[intIndex+1] & 0xFFFFFFFFL;
            long upperMask = (1L << spillBits) - 1;
            combined |= (upper & upperMask) << (32 - bitOffset);
        }

        long valueMask = nOfBitsPerValue >= 32 ? 0xFFFFFFFFL : (1L << nOfBitsPerValue) - 1;
        int value = (int) (combined & valueMask);

        int indicatorBit = 1 << (nOfBitsPerValue-1);

        if((value & indicatorBit)!=0){
            int overflowIndex = value & (indicatorBit - 1);
            int bitIndexOverflow = nOfBitsPerValueOverflow*overflowIndex;
            int intIndexOverflow = bitIndexOverflow/32;
            int bitOffsetOverflow = bitIndexOverflow % 32;
            
            long overflowLower = dataOverflowArea[intIndexOverflow] & 0xFFFFFFFFL;
            long overflowCombined = overflowLower >>> bitOffsetOverflow;

            if (nOfBitsPerValueOverflow > 32 - bitOffsetOverflow && intIndexOverflow + 1 < dataOverflowArea.length){
                int overflowSpill = nOfBitsPerValueOverflow - (32 - bitOffsetOverflow);
                long overflowUpper = dataOverflowArea[intIndexOverflow+1] & 0xFFFFFFFFL;
                long overflowMask = (1L << overflowSpill) - 1;
                overflowCombined |= (overflowUpper & overflowMask) << (32 - bitOffsetOverflow);
            }

            long overflowValueMask = nOfBitsPerValueOverflow >= 32 ? 0xFFFFFFFFL : (1L << nOfBitsPerValueOverflow) - 1;
            value = (int) (overflowCombined & overflowValueMask);
        }

        return value;
    }

    @Override
    public void decompress(int[] arr){
        for(int i=0; i<arr.length; i++){
            arr[i] = get(i);
        }

    }
}
