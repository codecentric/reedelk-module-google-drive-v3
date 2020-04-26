package com.reedelk.google.drive.v3.internal.command;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.PermissionList;
import com.reedelk.google.drive.v3.internal.commons.MapperPermission;
import com.reedelk.google.drive.v3.internal.exception.PermissionListException;
import com.reedelk.runtime.api.commons.StringUtils;
import com.reedelk.runtime.api.exception.PlatformException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.reedelk.google.drive.v3.internal.commons.Messages.PermissionList.FILE_ID_EMPTY;
import static com.reedelk.google.drive.v3.internal.commons.Messages.PermissionList.GENERIC_ERROR;
import static com.reedelk.runtime.api.commons.StringUtils.*;
import static java.lang.String.join;
import static java.util.stream.Collectors.toList;

public class PermissionListCommand implements Command<List<Map>> {

    private final String fileId;

    public PermissionListCommand(String fileId) {
        if (isBlank(fileId)) {
            throw new PermissionListException(FILE_ID_EMPTY.format());
        }
        this.fileId = fileId;
    }

    @Override
    public List<Map> execute(Drive drive) throws IOException {

        PermissionList list = drive.permissions().list(fileId)
                .setFields(join(",", MapperPermission.FIELDS))
                .execute();

        return list.getPermissions()
                .stream()
                .map(MapperPermission.GET)
                .collect(toList());
    }

    @Override
    public PlatformException onException(Exception exception) {
        String error = GENERIC_ERROR.format(fileId, exception.getMessage());
        return new PermissionListException(error, exception);
    }
}
