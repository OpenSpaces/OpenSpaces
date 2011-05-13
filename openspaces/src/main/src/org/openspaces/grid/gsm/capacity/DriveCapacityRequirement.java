package org.openspaces.grid.gsm.capacity;


public class DriveCapacityRequirement extends AbstractCapacityRequirement {

    private final String drive;
    
    public DriveCapacityRequirement(String drive) {
        super();
        this.drive = drive;
    }
    
    public DriveCapacityRequirement(String drive, Long sizeInMB) {
        super(sizeInMB);
        this.drive = drive;
    }

    public CapacityRequirementType<DriveCapacityRequirement> getType() {
        return new CapacityRequirementType<DriveCapacityRequirement>(this.getClass(), drive);
    }
    
    public long getDriveCapacityInMB() {
        return value;
    }
    
    public String toString() {
        return getDriveCapacityInMB() +"MB on " + drive;
    }

    /**
     * @return the root folder of this drive (Such as "/" on linux or "c:\" on windows)
     */
    public String getDrive() {
        return drive;
    }
}