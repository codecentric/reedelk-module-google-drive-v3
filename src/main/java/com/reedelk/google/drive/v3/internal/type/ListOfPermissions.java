package com.reedelk.google.drive.v3.internal.type;

import com.google.api.services.drive.model.Permission;
import com.reedelk.runtime.api.annotation.Type;

import java.util.ArrayList;
import java.util.List;

@Type(listItemType = PermissionType.class)
public class ListOfPermissions extends ArrayList<PermissionType> {

    public ListOfPermissions(List<Permission> list) {
        if (list != null) {
            list.stream().map(PermissionType::new).forEach(this::add);
        }
    }
}
