package org.openspaces.grid.esm;

import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.elastic.MemorySla;
import org.openspaces.admin.pu.elastic.MemorySlaSerializer;

public class SlaExtractor {
    
    private MemorySla memorySla = null;

    public SlaExtractor(ProcessingUnit pu) {
        String slaDescriptors = (String)pu.getBeanLevelProperties().getContextProperties().get("sla");
        if (slaDescriptors == null || slaDescriptors.length() == 0) {
            return; //no sla defined
        }
        
        //extract sla
        String[] split = slaDescriptors.split("/");
        for (String s : split) {
            int beginIndex = s.indexOf("sla=", 0)+"sla=".length();
            int endIndex = s.indexOf(',',beginIndex);
            String sla = s.substring(beginIndex, endIndex);
            if (sla.equals(MemorySla.class.getSimpleName())) {
                beginIndex = endIndex;
                endIndex = s.indexOf("sla=", endIndex);
                if (endIndex < 0) {
                    endIndex = s.length();
                }
                sla = s.substring(beginIndex, endIndex);
                memorySla = MemorySlaSerializer.fromString(sla);
            }
        }
    }
    
    /** returns the memory sla if present, null otherwise */
    public MemorySla getMemorySla() {
        return memorySla;
    }
    
}
