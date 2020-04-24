package com.reedelk.google.drive.v3.internal.attribute;

import java.io.Serializable;
import java.util.HashMap;

public class FileDownloadAttributes extends HashMap<String, Serializable> {

    private static final String ATTR_ID = "fileId";

    public FileDownloadAttributes(String fileId) {
        put(ATTR_ID, fileId);
    }
}
