package org.openspaces.admin.pu.elastic;

public class MemorySlaSerializer {
    static String toString(MemorySla memorySla) {
        String descriptor = "sla="+MemorySla.class.getSimpleName() + ",threshold="+(memorySla).getThreshold()+"%,subsetSize="+memorySla.getSubsetSize();
        return descriptor;
    }
    
    public static MemorySla fromString(String memorySla) {
        int beginIndex = memorySla.indexOf("threshold=")+"threshold=".length();
        int endIndex = memorySla.indexOf("%")+1;
        String threshold = memorySla.substring(beginIndex, endIndex);
        
        beginIndex = memorySla.indexOf("subsetSize=")+"subsetSize=".length();
        String subsetSizeStr = memorySla.substring(beginIndex);
        int subsetSize = Integer.valueOf(subsetSizeStr);
        MemorySla memorySlaObj = new MemorySla(threshold, subsetSize);
        return memorySlaObj;
    }
    
    public static void main(String[] args) {
        MemorySla sla = new MemorySla("75%", 6);
        System.out.println(sla.getThreshold());
        System.out.println(sla.getSubsetSize());
        
        String string = MemorySlaSerializer.toString(sla);
        MemorySla out = MemorySlaSerializer.fromString(string);
        System.out.println(out.getThreshold());
        System.out.println(out.getSubsetSize());
    }
}
