package com.reedelk.google.drive.v3.internal.attribute;

import com.google.api.services.drive.model.File;
import com.reedelk.runtime.api.annotation.Type;
import com.reedelk.runtime.api.annotation.TypeProperty;
import com.reedelk.runtime.api.message.MessageAttributes;

import java.util.List;

import static com.reedelk.google.drive.v3.internal.attribute.FileUploadAttributes.*;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

@Type
@TypeProperty(name = WEB_CONTENT_LINK, type = String.class)
@TypeProperty(name = WEB_VIEW_LINK, type = String.class)
@TypeProperty(name = NAME, type = String.class)
@TypeProperty(name = ID, type = String.class)
public class FileUploadAttributes extends MessageAttributes {

    static final String WEB_CONTENT_LINK = "webContentLink";
    static final String WEB_VIEW_LINK = "webViewLink";
    static final String NAME = "name";
    static final String ID = "id";

    public static final List<String> ALL_ATTRIBUTES =
            unmodifiableList(asList(WEB_CONTENT_LINK, WEB_VIEW_LINK, NAME, ID));

    public FileUploadAttributes(File file) {
        put(WEB_CONTENT_LINK, file.getWebContentLink());
        put(WEB_VIEW_LINK, file.getWebViewLink());
        put(NAME, file.getName());
        put(ID, file.getId());
    }
}
