package com.reedelk.google.drive.v3.internal.command;

import com.google.api.services.drive.Drive;
import com.reedelk.google.drive.v3.internal.commons.Messages.PermissionDelete;
import com.reedelk.google.drive.v3.internal.exception.PermissionDeleteException;
import com.reedelk.runtime.api.exception.PlatformException;

import java.io.IOException;

import static com.reedelk.google.drive.v3.internal.commons.Messages.PermissionDelete.*;
import static com.reedelk.google.drive.v3.internal.commons.Messages.PermissionDelete.GENERIC_ERROR;
import static com.reedelk.runtime.api.commons.StringUtils.isBlank;

public class PermissionDeleteCommand implements Command<Void> {

    private final String permissionId;
    private final String fileId;

    public PermissionDeleteCommand(String permissionId, String fileId) {
        if (isBlank(permissionId)) {
            throw new PermissionDeleteException(PERMISSION_ID_EMPTY.format());
        }
        if (isBlank(fileId)) {
            throw new PermissionDeleteException(FILE_ID_EMPTY.format());
        }
        this.permissionId = permissionId;
        this.fileId = fileId;
    }

    @Override
    public Void execute(Drive drive) throws IOException {
        drive.permissions()
                .delete(fileId, permissionId)
                .execute();
        return null;
    }

    @Override
    public PlatformException onException(Exception exception) {
        String error = GENERIC_ERROR.format(permissionId, fileId, exception.getMessage());
        return new PermissionDeleteException(error, exception);
    }
}
