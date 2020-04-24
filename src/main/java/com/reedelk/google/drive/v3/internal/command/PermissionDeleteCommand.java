package com.reedelk.google.drive.v3.internal.command;

import com.google.api.services.drive.Drive;
import com.reedelk.google.drive.v3.internal.exception.PermissionDeleteException;
import com.reedelk.runtime.api.commons.StackTraceUtils;
import com.reedelk.runtime.api.exception.PlatformException;

import java.io.IOException;

public class PermissionDeleteCommand implements Command<Void> {

    private final String permissionId;
    private final String fileId;

    public PermissionDeleteCommand(String permissionId, String fileId) {
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
        String error = exception.getMessage(); // TODO:
        return new PermissionDeleteException(error, exception);
    }
}
