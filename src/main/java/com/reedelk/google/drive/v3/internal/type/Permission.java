package com.reedelk.google.drive.v3.internal.type;

import com.reedelk.runtime.api.annotation.Type;
import com.reedelk.runtime.api.annotation.TypeProperty;

import java.io.Serializable;
import java.util.HashMap;

import static com.reedelk.google.drive.v3.internal.type.Permission.*;

@Type(displayName = "Permission", mapKeyType = String.class, mapValueType = Serializable.class)
@TypeProperty(name = ID, type = String.class)
@TypeProperty(name = TYPE, type = String.class)
@TypeProperty(name = ROLE, type = String.class)
@TypeProperty(name = DOMAIN, type = String.class)
@TypeProperty(name = DISPLAY_NAME, type = String.class)
@TypeProperty(name = EMAIL_ADDRESS, type = String.class)
public class Permission extends HashMap<String, Serializable> {

    static final String ID = "id";
    static final String TYPE = "type";
    static final String ROLE = "role";
    static final String DOMAIN = "domain";
    static final String DISPLAY_NAME = "displayName";
    static final String EMAIL_ADDRESS = "emailAddress";

    public Permission(com.google.api.services.drive.model.Permission permission) {
        put(ID, permission.getId());
        put(TYPE, permission.getType());
        put(ROLE, permission.getRole());
        put(DOMAIN, permission.getDomain());
        put(DISPLAY_NAME, permission.getDomain());
        put(EMAIL_ADDRESS, permission.getEmailAddress());
    }
}
