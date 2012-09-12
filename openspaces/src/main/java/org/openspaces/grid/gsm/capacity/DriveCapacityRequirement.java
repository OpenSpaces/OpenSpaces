/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
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
        return getDriveCapacityInMB() +"MB disk on " + drive;
    }

    /**
     * @return the root folder of this drive (Such as "/" on linux or "c:\" on windows)
     */
    public String getDrive() {
        return drive;
    }
}
