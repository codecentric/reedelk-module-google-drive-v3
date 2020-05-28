package com.reedelk.google.drive.v3.internal.type;

import com.reedelk.runtime.api.annotation.Type;
import com.reedelk.runtime.api.annotation.TypeProperty;

import java.io.Serializable;
import java.util.HashMap;

@Type(displayName = "User", mapKeyType = String.class, mapValueType = Serializable.class)
@TypeProperty(name = User.EMAIL_ADDRESS, type = String.class)
public class User extends HashMap<String, Serializable> {

    static final String EMAIL_ADDRESS = "emailAddress";

    public User(com.google.api.services.drive.model.User user) {
        put(EMAIL_ADDRESS, user.getEmailAddress());
    }
}
