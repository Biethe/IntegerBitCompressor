class NegativeIntegerBitPacking extends OverflowBitPacking{
    @Override
    public int[][] arraySplitter(int[] arr){
        int[] normalArea = arr.clone();
        int[] overflowArea;
        int sizeOfOverflowArea=0;

        
        for(int i = 0; i<arr.length; i++){
            if(arr[i] < 0){
               sizeOfOverflowArea++;
            }
        }

        overflowArea = new int[sizeOfOverflowArea];
        int newNegValue = 0;
        for(int i = 0; i<arr.length; i++){
            if(arr[i] < 0){
               overflowArea[newNegValue] = (int) Math.abs(arr[i]);
               normalArea[i] = newNegValue;
               newNegValue++;
            }
        }
        return new int[][] {normalArea, overflowArea};
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
            int overflowValue = (int) (overflowCombined & overflowValueMask);
            return -overflowValue;
        }

        return value;
    }

    
}
