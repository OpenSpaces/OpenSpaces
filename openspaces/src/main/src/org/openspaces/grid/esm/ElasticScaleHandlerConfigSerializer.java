package org.openspaces.grid.esm;

import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

class ElasticScaleHandlerConfigSerializer {
    
    static String toString(ElasticScaleHandlerConfig config) {
        StringBuilder out = new StringBuilder();
        out.append("<class>").append(config.getClassName()).append("</class>");
        Properties properties = config.getProperties();
        if (!properties.isEmpty()) {
            out.append("<props>");
            Set<Entry<Object,Object>> entrySet = properties.entrySet();
            for (Entry<Object,Object> entry : entrySet) {
                out.append("<key>").append(entry.getKey()).append("</key>");
                out.append("<val>").append(entry.getValue()).append("</val>");
            }
            out.append("</props>");
        }
        return out.toString();
    }
    
    static ElasticScaleHandlerConfig fromString(String config) {
        
        int s,e;
        s = config.indexOf("<class>") + "<class>".length();
        e = config.indexOf("</class>");
        String classname = config.substring(s, e);
        ElasticScaleHandlerConfig elasticConfig = new ElasticScaleHandlerConfig(classname);
        s = config.indexOf("<props>",e) + "<props>".length();
        e = config.indexOf("</props>",e);
        if (e != -1) {
            String properties = config.substring(s, e);
            int s0 = 0, e0 = 0;
            while (e0 != -1) {
                s0 = properties.indexOf("<key>",e0) + "<key>".length();
                e0 = properties.indexOf("</key>",e0);
                if (e0 != -1) {
                    String key = properties.substring(s0, e0);
                    e0 = e0 + "</key>".length();
                    s0 = properties.indexOf("<val>",e0) + "<val>".length();
                    e0 = properties.indexOf("</val>",e0);
                    if (e0 != -1) {
                        String val = properties.substring(s0, e0);
                        e0 = e0 + "</val>".length();
                        elasticConfig.addProperty(key, val);
                    }
                }
            }
        }
        return elasticConfig;
    }
    
    
    public static void main(String[] args) {
        String s = ElasticScaleHandlerConfigSerializer.toString(new ElasticScaleHandlerConfig("myclass").addProperty("machines",
                "pc-lab12,pc-lab13,pc-lab15").addProperty("exclude", "pc-lab1").addProperty("timeout", "1000").addProperty("ports", "4000,5000,6000"));
        System.out.println(s);
        
        ElasticScaleHandlerConfig fromString = ElasticScaleHandlerConfigSerializer.fromString(s);
        System.out.println(ElasticScaleHandlerConfigSerializer.toString(fromString));
    }
}
