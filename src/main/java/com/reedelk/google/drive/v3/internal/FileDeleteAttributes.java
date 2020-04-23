package com.reedelk.google.drive.v3.internal;

import java.io.Serializable;
import java.util.HashMap;

public class FileDeleteAttributes extends HashMap<String, Serializable> {

    public FileDeleteAttributes(String fileId) {
        put("fileId", fileId);
    }
}
