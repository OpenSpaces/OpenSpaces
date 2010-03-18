package org.openspaces.grid.esm;

import org.openspaces.admin.esm.deployment.MemorySla;
import org.openspaces.admin.pu.ProcessingUnit;

public class SlaExtractor {
    
    private MemorySla memorySla = null;

    public SlaExtractor(ProcessingUnit pu) {
        String slaDescriptors = (String)pu.getBeanLevelProperties().getContextProperties().get("sla");
        if (slaDescriptors == null) {
            return; //no sla defined
        }
        
        //extract sla
        String[] split = slaDescriptors.split("/");
        for (String s : split) {
            int beginIndex = s.indexOf("sla=", 0)+"sla=".length();
            int endIndex = s.indexOf(',',beginIndex);
            String sla = s.substring(beginIndex, endIndex);
            if (sla.equals(MemorySla.class.getSimpleName())) {
                beginIndex = s.indexOf("threshold=", endIndex)+"threshold=".length();
                String threshold = s.substring(beginIndex);
                memorySla = new MemorySla(threshold);
            }
        }
    }
    
    /** returns the memory sla if present, null otherwise */
    public MemorySla getMemorySla() {
        return memorySla;
    }
    
}
