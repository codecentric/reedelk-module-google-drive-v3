package com.reedelk.google.drive.v3.internal.attribute;

import java.io.Serializable;
import java.util.HashMap;

public class PermissionDeleteAttribute extends HashMap<String, Serializable> {

    private static final String ATTR_ID = "permissionId";
    private static final String ATTR_FILE_ID = "fileId";

    public PermissionDeleteAttribute(String permissionId, String fileId) {
        put(ATTR_ID, permissionId);
        put(ATTR_FILE_ID, fileId);
    }
}
