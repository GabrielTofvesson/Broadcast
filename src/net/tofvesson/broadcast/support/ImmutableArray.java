package net.tofvesson.broadcast.support;

import java.util.Arrays;

/**
 * Wrapper for arrays to prevent modification
 * @param <T> Element type
 */
public class ImmutableArray<T> {

    protected final T[] array;
    protected ImmutableArray(T[] array){ this.array = array; }

    public int length(){ return array.length; }
    public T at(int index){ return array[index]; }

    @Override
    public boolean equals(Object obj) {
        Object[] compareTo;
        if(obj instanceof ImmutableArray) compareTo = ((ImmutableArray) obj).array;
        else if(obj!=null && obj.getClass().isArray()){
            try{
                compareTo = (Object[]) obj;
            }catch(ClassCastException e){
                return false; // Object was a primitive array
            }
        }
        else return false;
        return Arrays.equals(array, compareTo);
    }

    public boolean compare(ImmutableArray<T> to, int targetOffset, int offset, int length){
        if(offset+length>array.length || targetOffset+length>to.array.length) return false;
        for(int i = 0; i<length; ++i)
            if(!areEqual(to.array[i+targetOffset], array[i+offset]))
                return false;
        return true;
    }

    protected static boolean areEqual(Object o1, Object o2){
        return o1==o2 || (o1!=null && o1.equals(o2));
    }

    public static <T> ImmutableArray<T> from(T[] t){ return new ImmutableArray<>(t); }
    public static ImmutableArray<Boolean> from(boolean[] t){
        Boolean[] t1 = new Boolean[t.length];
        for(int i = 0; i<t1.length; ++i) t1[i] = t[i];
        return new ImmutableArray<>(t1);
    }
    public static ImmutableArray<Character> from(char[] t){
        Character[] t1 = new Character[t.length];
        for(int i = 0; i<t1.length; ++i) t1[i] = t[i];
        return new ImmutableArray<>(t1);
    }
    public static ImmutableArray<Byte> from(byte[] t){
        Byte[] t1 = new Byte[t.length];
        for(int i = 0; i<t1.length; ++i) t1[i] = t[i];
        return new ImmutableArray<>(t1);
    }
    public static ImmutableArray<Short> from(short[] t){
        Short[] t1 = new Short[t.length];
        for(int i = 0; i<t1.length; ++i) t1[i] = t[i];
        return new ImmutableArray<>(t1);
    }
    public static ImmutableArray<Integer> from(int[] t){
        Integer[] t1 = new Integer[t.length];
        for(int i = 0; i<t1.length; ++i) t1[i] = t[i];
        return new ImmutableArray<>(t1);
    }
    public static ImmutableArray<Long> from(long[] t){
        Long[] t1 = new Long[t.length];
        for(int i = 0; i<t1.length; ++i) t1[i] = t[i];
        return new ImmutableArray<>(t1);
    }
    public static ImmutableArray<Float> from(float[] t){
        Float[] t1 = new Float[t.length];
        for(int i = 0; i<t1.length; ++i) t1[i] = t[i];
        return new ImmutableArray<>(t1);
    }
    public static ImmutableArray<Double> from(double[] t){
        Double[] t1 = new Double[t.length];
        for(int i = 0; i<t1.length; ++i) t1[i] = t[i];
        return new ImmutableArray<>(t1);
    }
}
