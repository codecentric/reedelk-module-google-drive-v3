package com.reedelk.google.drive.v3.internal.attribute;

import com.reedelk.runtime.api.annotation.Type;
import com.reedelk.runtime.api.annotation.TypeProperty;
import com.reedelk.runtime.api.message.MessageAttributes;

import static com.reedelk.google.drive.v3.internal.attribute.PermissionDeleteAttribute.FILE_ID;
import static com.reedelk.google.drive.v3.internal.attribute.PermissionDeleteAttribute.ID;

@Type
@TypeProperty(name = ID, type = String.class)
@TypeProperty(name = FILE_ID, type = String.class)
public class PermissionDeleteAttribute extends MessageAttributes {

    static final String ID = "id";
    static final String FILE_ID = "fileId";

    public PermissionDeleteAttribute(String permissionId, String fileId) {
        put(ID, permissionId);
        put(FILE_ID, fileId);
    }
}
