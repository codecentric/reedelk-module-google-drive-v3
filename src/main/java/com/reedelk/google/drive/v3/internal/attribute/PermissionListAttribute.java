package com.reedelk.google.drive.v3.internal.attribute;

import java.io.Serializable;
import java.util.HashMap;

public class PermissionListAttribute extends HashMap<String, Serializable> {

    private static final String ATTR_FILE_ID = "fileId";

    public PermissionListAttribute(String fileId) {
        put(ATTR_FILE_ID, fileId);
    }
}
