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
            if(Integer.numberOfLeadingZeros(arr[i]) == 0){
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

            data[intIndex] |= (normalArea[i] << bitOffset);
            if(normalArea[i] != arr[i]){
                data[intIndex] |= (1 << (nOfBitsPerValue-1)) << (bitOffset);
            }   
            // Handle cross-integer bitpacking

            if (nOfBitsPerValue > (32 - bitOffset) && normalArea[i] == arr[i]){
                data[intIndex + 1] |= (arr[i] >> (32 - bitOffset));
                
            }   
            if(nOfBitsPerValue > (32 - bitOffset) && normalArea[i] != arr[i]){
                data[intIndex+1] |= 1<<bitOffset+nOfBitsPerValue-33;
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

        int value = (data[intIndex] & (((1 << nOfBitsPerValue)-1) << bitOffset))>> bitOffset;     
        
        
        // Handle cross-integer bitpacked integers
        if (nOfBitsPerValue > 32 - bitOffset){
            int mask = (1 << (bitOffset + nOfBitsPerValue - 32))-1;
            value &= ((1 << (32-bitOffset)) - 1); //In case value has leading ones caused by the operation >> bitOffet
            value |= ((data[intIndex+1] & mask) << (32- bitOffset));
        }
        
        if((value >> (nOfBitsPerValue-1))==1){
            // System.out.println(value);
            value &= ((1 << (nOfBitsPerValue-1))-1);
            int bitIndexOverflow = nOfBitsPerValueOverflow*value;
            int intIndexOverflow = bitIndexOverflow/32;
            int bitOffsetOverflow = bitIndexOverflow % 32;
            
            value = (dataOverflowArea[intIndexOverflow] & (((1 << nOfBitsPerValueOverflow)-1) << bitOffsetOverflow))>> bitOffsetOverflow;
            // Handle cross-integer bitpacked integers
            if (nOfBitsPerValueOverflow > 32 - bitOffsetOverflow){
                int mask = (1 << (bitOffsetOverflow + nOfBitsPerValueOverflow - 32))-1;
                value &= ((1 << (32-bitOffsetOverflow)) - 1); //In case value has leading ones caused by the operation >> bitOffet
                value |= ((dataOverflowArea[intIndexOverflow+1] & mask) << (32- bitOffsetOverflow));
            }  
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