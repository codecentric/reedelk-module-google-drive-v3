package com.reedelk.google.drive.v3.internal.attribute;

import com.google.api.services.drive.model.File;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class FolderCreateAttributes extends HashMap<String, Serializable> {

    private static final String ATTR_WEB_CONTENT_LINK = "webContentLink";
    private static final String ATTR_WEB_VIEW_LINK = "webViewLink";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_ID = "id";

    public static final List<String> ALL_ATTRIBUTES =
            Arrays.asList(ATTR_WEB_CONTENT_LINK, ATTR_WEB_VIEW_LINK, ATTR_NAME, ATTR_ID);

    public FolderCreateAttributes(File file) {
        put(ATTR_WEB_CONTENT_LINK, file.getWebContentLink());
        put(ATTR_WEB_VIEW_LINK, file.getWebViewLink());
        put(ATTR_NAME, file.getName());
        put(ATTR_ID, file.getId());
    }
}
