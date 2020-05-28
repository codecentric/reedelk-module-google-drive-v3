package com.reedelk.google.drive.v3.internal.type;

import com.reedelk.runtime.api.annotation.Type;

import java.util.ArrayList;
import java.util.List;

@Type(listItemType = Permission.class)
public class ListOfPermissions extends ArrayList<Permission> {

    public ListOfPermissions(List<com.google.api.services.drive.model.Permission> list) {
        if (list != null) {
            list.stream().map(Permission::new).forEach(this::add);
        }
    }
}
