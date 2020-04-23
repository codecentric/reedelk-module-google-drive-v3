package com.reedelk.google.drive.v3.internal;

import java.io.Serializable;
import java.util.HashMap;

public class FileDeleteAttributes extends HashMap<String, Serializable> {

    private static final String ATTR_ID = "fileId";

    public FileDeleteAttributes(String fileId) {
        put(ATTR_ID, fileId);
    }
}
