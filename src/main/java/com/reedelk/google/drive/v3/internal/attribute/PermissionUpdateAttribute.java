package com.reedelk.google.drive.v3.internal.attribute;

import com.google.api.services.drive.model.Permission;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PermissionUpdateAttribute extends HashMap<String, Serializable> {

    private static final String ATTR_FILE_ID = "fileId";
    private static final String ATTR_ID = "id";
    private static final String ATTR_KIND = "kind";
    private static final String ATTR_ROLE = "role";
    private static final String ATTR_TYPE = "type";

    public static final List<String> ALL_ATTRIBUTES =
            Arrays.asList(ATTR_ID, ATTR_KIND, ATTR_ROLE, ATTR_TYPE);

    public PermissionUpdateAttribute(String fileId, Permission permission) {
        put(ATTR_ID, permission.getId());
        put(ATTR_KIND, permission.getKind());
        put(ATTR_ROLE, permission.getRole());
        put(ATTR_TYPE, permission.getType());
        put(ATTR_FILE_ID, fileId);
    }
}
