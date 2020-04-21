package com.reedelk.google.drive.component;

import com.google.api.services.drive.model.Permission;
import com.reedelk.runtime.api.annotation.DisplayName;

public enum PermissionRole {

    @DisplayName("Owner")
    OWNER("owner"),
    @DisplayName("Organizer")
    ORGANIZER("organizer"),
    @DisplayName("File Organizer")
    FILE_ORGANIZER("fileOrganizer"),
    @DisplayName("Writer")
    WRITER("writer"),
    @DisplayName("Commenter")
    COMMENTER("commenter"),
    @DisplayName("Reader")
    READER("reader");

    private final String value;

    PermissionRole(String value) {
        this.value = value;
    }

    public void set(Permission permission) {
        permission.setRole(value);
    }
}
