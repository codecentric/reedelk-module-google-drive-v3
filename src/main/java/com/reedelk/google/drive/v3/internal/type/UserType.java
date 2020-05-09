package com.reedelk.google.drive.v3.internal.type;

import com.google.api.services.drive.model.User;
import com.reedelk.runtime.api.annotation.Type;
import com.reedelk.runtime.api.annotation.TypeProperty;

import java.io.Serializable;
import java.util.HashMap;

@Type
@TypeProperty(name = UserType.EMAIL_ADDRESS, type = String.class)
public class UserType extends HashMap<String, Serializable> {

    static final String EMAIL_ADDRESS = "emailAddress";

    public UserType(User user) {
        put(EMAIL_ADDRESS, user.getEmailAddress());
    }
}
