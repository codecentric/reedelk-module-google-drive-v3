package com.reedelk.google.drive.v3.internal.type;

import com.google.api.services.drive.model.User;
import com.reedelk.runtime.api.annotation.Type;

import java.io.Serializable;
import java.util.HashMap;

@Type
public class UserType extends HashMap<String, Serializable> {

    public UserType(User user) {
        put("emailAddress", user.getEmailAddress());
    }
}
