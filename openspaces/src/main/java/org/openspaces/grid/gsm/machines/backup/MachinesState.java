package org.openspaces.grid.gsm.machines.backup;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceDynamicProperties;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.document.DocumentProperties;

@SpaceClass
public class MachinesState {

    public static final Integer SINGLETON_ID = 0;
    
    private Integer id;
    private DocumentProperties properties;
    private Long version;

    public MachinesState() {
    }
    
    @SpaceId(autoGenerate = false)
    public Integer getId() {
        return id;
    }

    
    public void setId(Integer id) {
        this.id = id;
    }

    @SpaceDynamicProperties
    public DocumentProperties getProperties() {
        return properties;
    }

    public void setProperties(DocumentProperties properties) {
        this.properties = properties;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

}
