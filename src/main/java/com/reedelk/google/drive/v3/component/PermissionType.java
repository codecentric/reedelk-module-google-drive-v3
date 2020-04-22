package com.reedelk.google.drive.v3.component;

import com.google.api.services.drive.model.Permission;
import com.reedelk.runtime.api.annotation.DisplayName;

public enum PermissionType {
    /**
     * https://developers.google.com/drive/api/v3/reference/permissions/create
     * When creating a permission, if type is user or group, you must provide an
     * emailAddress for the user or group. When type is domain, you must provide a domain.
     * There isn't extra information required for a anyone type.
     */
    @DisplayName("User")
    USER("user"),
    @DisplayName("Group")
    GROUP("group"),
    @DisplayName("Domain")
    DOMAIN("domain"),
    @DisplayName("Anyone")
    ANYONE("anyone");

    private final String value;

    PermissionType(String value) {
        this.value = value;
    }

    public void set(Permission permission) {
        permission.setType(value);
    }
}
