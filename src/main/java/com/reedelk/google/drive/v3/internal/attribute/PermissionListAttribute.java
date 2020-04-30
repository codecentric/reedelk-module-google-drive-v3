package com.reedelk.google.drive.v3.internal.attribute;

import com.reedelk.runtime.api.annotation.Type;
import com.reedelk.runtime.api.annotation.TypeProperty;
import com.reedelk.runtime.api.message.MessageAttributes;

import static com.reedelk.google.drive.v3.internal.attribute.PermissionListAttribute.FILE_ID;

@Type
@TypeProperty(name = FILE_ID, type = String.class)
public class PermissionListAttribute extends MessageAttributes {

    static final String FILE_ID = "fileId";

    public PermissionListAttribute(String fileId) {
        put(FILE_ID, fileId);
    }
}
