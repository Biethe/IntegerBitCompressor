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
        int nOfIntegersPerPositiveInt = 32/(nOfBitsPerValue);
        int PositiveIntIndex = index/nOfIntegersPerPositiveInt;
        int PositiveBitIndex = (index % nOfIntegersPerPositiveInt);
        int nOfIntegersPerIntNegative = 32/(nOfBitsPerValueOverflow);
        
        

        int value = (data[PositiveIntIndex] & ((1 << (nOfBitsPerValue))-1)<<(PositiveBitIndex*(nOfBitsPerValue))) >> (PositiveBitIndex*(nOfBitsPerValue));
        //In case we have leading ones which might happen a few times within the array.
        value &= ((1 << (nOfBitsPerValue)) - 1);
        
        if((value >> (nOfBitsPerValue-1))==1){
            value &= ((1 << (nOfBitsPerValue-1))-1);
            int intIndexNegative = value/nOfIntegersPerIntNegative;
            int bitIndexNegative = (value % nOfIntegersPerIntNegative);
            value = -(dataOverflowArea[intIndexNegative] & (((1 << nOfBitsPerValueOverflow)-1)<<(bitIndexNegative*nOfBitsPerValueOverflow))) >> (bitIndexNegative*nOfBitsPerValueOverflow);
        }

        return value;
    }

    
}