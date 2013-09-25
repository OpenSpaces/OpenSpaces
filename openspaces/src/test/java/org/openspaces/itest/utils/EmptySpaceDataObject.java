package org.openspaces.itest.utils;

import com.gigaspaces.annotation.pojo.SpaceId;

/**
 * Used for unit testing, empty space object
 * @author eitany
 *
 */
public class EmptySpaceDataObject {
    private String uid;

    @SpaceId(autoGenerate=true)
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
