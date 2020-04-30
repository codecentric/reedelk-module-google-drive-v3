package com.reedelk.google.drive.v3.internal.attribute;

import com.google.api.services.drive.model.Permission;
import com.reedelk.runtime.api.annotation.Type;
import com.reedelk.runtime.api.annotation.TypeProperty;
import com.reedelk.runtime.api.message.MessageAttributes;

import java.util.List;

import static com.reedelk.google.drive.v3.internal.attribute.PermissionUpdateAttribute.*;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

@Type
@TypeProperty(name = FILE_ID, type = String.class)
@TypeProperty(name = ID, type = String.class)
@TypeProperty(name = KIND, type = String.class)
@TypeProperty(name = ROLE, type = String.class)
@TypeProperty(name = TYPE, type = String.class)
public class PermissionUpdateAttribute extends MessageAttributes {

    static final String FILE_ID = "fileId";
    static final String ID = "id";
    static final String KIND = "kind";
    static final String ROLE = "role";
    static final String TYPE = "type";

    public static final List<String> ALL_ATTRIBUTES =
            unmodifiableList(asList(ID, KIND, ROLE, TYPE));

    public PermissionUpdateAttribute(String fileId, Permission permission) {
        put(ID, permission.getId());
        put(KIND, permission.getKind());
        put(ROLE, permission.getRole());
        put(TYPE, permission.getType());
        put(FILE_ID, fileId);
    }
}
