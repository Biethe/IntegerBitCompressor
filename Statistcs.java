import java.lang.reflect.Array;

class Statistics{
    protected double mean;
    protected double std;
    protected double sparsity;

    private Object cachedArray;
    private int cachedLength;
    private boolean cacheValid;

    public double computeMean(Object array){
        ensureStatistics(array);
        return mean;
    }

    public double computeStd(Object array){
        ensureStatistics(array);
        return std;
    }

    public double computeSparsity(Object array){
        ensureStatistics(array);
        return sparsity;
    }

    private void ensureStatistics(Object array){
        int length = validateArrayInput(array);
        if(cacheValid && cachedArray == array && cachedLength == length){
            return;
        }
        StatAccumulator accumulator = new StatAccumulator();
        if(array instanceof double[]){
            accumulate((double[]) array, accumulator);
        }else if(array instanceof int[]){
            accumulate((int[]) array, accumulator);
        }else if(array instanceof long[]){
            accumulate((long[]) array, accumulator);
        }else if(array instanceof float[]){
            accumulate((float[]) array, accumulator);
        }else if(array instanceof short[]){
            accumulate((short[]) array, accumulator);
        }else if(array instanceof byte[]){
            accumulate((byte[]) array, accumulator);
        }else if(array instanceof Number[]){
            accumulate((Number[]) array, accumulator);
        }else{
            accumulateUsingReflection(array, length, accumulator);
        }

        mean = accumulator.mean();
        std = accumulator.std();
        sparsity = computeSparsity(mean, std);
        cachedArray = array;
        cachedLength = length;
        cacheValid = true;
    }

    private void accumulate(double[] values, StatAccumulator accumulator){
        for(double value : values){
            accumulator.add(value);
        }
    }

    private void accumulate(int[] values, StatAccumulator accumulator){
        for(int value : values){
            accumulator.add(value);
        }
    }

    private void accumulate(long[] values, StatAccumulator accumulator){
        for(long value : values){
            accumulator.add(value);
        }
    }

    private void accumulate(float[] values, StatAccumulator accumulator){
        for(float value : values){
            accumulator.add(value);
        }
    }

    private void accumulate(short[] values, StatAccumulator accumulator){
        for(short value : values){
            accumulator.add(value);
        }
    }

    private void accumulate(byte[] values, StatAccumulator accumulator){
        for(byte value : values){
            accumulator.add(value);
        }
    }

    private void accumulate(Number[] values, StatAccumulator accumulator){
        for(Number value : values){
            if(value == null){
                throw new IllegalArgumentException("Array elements must be numeric. Found: null");
            }
            accumulator.add(value.doubleValue());
        }
    }

    private void accumulateUsingReflection(Object array, int length, StatAccumulator accumulator){
        for(int i = 0; i < length; i++){
            accumulator.add(toDouble(Array.get(array, i)));
        }
    }

    private double computeSparsity(double meanValue, double stdValue){
        if(meanValue == 0.0){
            return Double.POSITIVE_INFINITY;
        }
        double result = stdValue / meanValue;
        return Double.isFinite(result) ? Math.abs(result) : Double.POSITIVE_INFINITY;
    }

    private int validateArrayInput(Object array){
        if(array == null || !array.getClass().isArray()){
            throw new IllegalArgumentException("Expected a non-null array.");
        }
        int length = Array.getLength(array);
        if(length == 0){
            throw new IllegalArgumentException("Array must contain at least one value.");
        }
        return length;
    }

    private double toDouble(Object value){
        if(!(value instanceof Number)){
            throw new IllegalArgumentException("Array elements must be numeric. Found: " + value);
        }
        return ((Number) value).doubleValue();
    }

    private static final class StatAccumulator{
        private int count;
        private double mean;
        private double m2;

        void add(double value){
            count++;
            double delta = value - mean;
            mean += delta / count;
            double delta2 = value - mean;
            m2 += delta * delta2;
        }

        double mean(){
            return mean;
        }

        double std(){
            if(count == 0){
                return 0.0;
            }
            return Math.sqrt(m2 / count);
        }
    }

}
