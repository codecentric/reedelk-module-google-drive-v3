package com.reedelk.google.drive.v3.internal.type;

import com.google.api.services.drive.model.User;
import com.reedelk.runtime.api.annotation.Type;

import java.util.ArrayList;
import java.util.List;

@Type(listItemType = UserType.class)
public class ListOfUsers extends ArrayList<UserType> {

    public ListOfUsers(List<User> users) {
        if (users != null) {
            users.stream().map(UserType::new).forEach(this::add);
        }
    }
}
