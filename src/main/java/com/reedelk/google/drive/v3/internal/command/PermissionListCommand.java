package com.reedelk.google.drive.v3.internal.command;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.PermissionList;
import com.reedelk.google.drive.v3.internal.exception.PermissionListException;
import com.reedelk.google.drive.v3.internal.type.ListOfPermissions;
import com.reedelk.runtime.api.exception.PlatformException;

import java.io.IOException;

import static com.reedelk.google.drive.v3.internal.commons.Messages.PermissionList.FILE_ID_EMPTY;
import static com.reedelk.google.drive.v3.internal.commons.Messages.PermissionList.GENERIC_ERROR;
import static com.reedelk.runtime.api.commons.StringUtils.isBlank;

public class PermissionListCommand implements Command<ListOfPermissions> {

    private final String fileId;

    public PermissionListCommand(String fileId) {
        if (isBlank(fileId)) {
            throw new PermissionListException(FILE_ID_EMPTY.format());
        }
        this.fileId = fileId;
    }

    @Override
    public ListOfPermissions execute(Drive drive) throws IOException {

        PermissionList list = drive.permissions().list(fileId)
                .setFields("*")
                .execute();

        return new ListOfPermissions(list.getPermissions());
    }

    @Override
    public PlatformException onException(Exception exception) {
        String error = GENERIC_ERROR.format(fileId, exception.getMessage());
        return new PermissionListException(error, exception);
    }
}
