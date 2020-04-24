package com.reedelk.google.drive.v3.internal.command;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.PermissionList;
import com.reedelk.google.drive.v3.internal.commons.Mappers;
import com.reedelk.google.drive.v3.internal.exception.PermissionListException;
import com.reedelk.runtime.api.exception.PlatformException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class PermissionListCommand implements Command<List<Map>> {

    private final String fileId;

    public PermissionListCommand(String fileId) {
        this.fileId = fileId;
    }

    @Override
    public List<Map> execute(Drive drive) throws IOException {

        PermissionList list = drive.permissions().list(fileId)
                .setFields("*")
                .execute();

        return list.getPermissions()
                .stream()
                .map(Mappers.PERMISSION)
                .collect(toList());
    }

    @Override
    public PlatformException onException(Exception exception) {
        String error = ""; // TODO:
        return new PermissionListException(error, exception);
    }
}
