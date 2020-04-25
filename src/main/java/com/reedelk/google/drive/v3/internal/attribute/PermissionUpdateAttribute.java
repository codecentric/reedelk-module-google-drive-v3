package com.reedelk.google.drive.v3.internal.attribute;

import java.io.Serializable;
import java.util.HashMap;

public class PermissionUpdateAttribute extends HashMap<String, Serializable> {

    private static final String ATTR_FILE_ID = "fileId";
    private static final String ATTR_ID = "id";

    public PermissionUpdateAttribute(String permissionId, String fileId) {
        put(ATTR_ID, permissionId);
        put(ATTR_FILE_ID, fileId);
    }
}
