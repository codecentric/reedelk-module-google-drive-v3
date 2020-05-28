package com.reedelk.google.drive.v3.internal.type;

import com.reedelk.runtime.api.annotation.Type;

import java.util.ArrayList;
import java.util.List;

@Type(listItemType = User.class)
public class ListOfUsers extends ArrayList<User> {

    public ListOfUsers(List<com.google.api.services.drive.model.User> users) {
        if (users != null) {
            users.stream().map(User::new).forEach(this::add);
        }
    }
}
