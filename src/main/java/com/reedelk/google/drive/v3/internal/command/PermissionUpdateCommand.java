package com.reedelk.google.drive.v3.internal.command;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;
import com.reedelk.google.drive.v3.component.PermissionRole;
import com.reedelk.google.drive.v3.internal.attribute.PermissionUpdateAttribute;
import com.reedelk.google.drive.v3.internal.exception.PermissionUpdateException;
import com.reedelk.runtime.api.exception.PlatformException;

import java.io.IOException;

import static com.reedelk.google.drive.v3.internal.commons.Messages.PermissionUpdate.*;
import static com.reedelk.runtime.api.commons.StringUtils.isBlank;
import static java.lang.String.join;

public class PermissionUpdateCommand implements Command<Permission> {

    private final String fileId;
    private final String permissionId;
    private final PermissionRole role;

    public PermissionUpdateCommand(String fileId,
                                   String permissionId,
                                   PermissionRole role) {
        if (isBlank(fileId)) {
            throw new PermissionUpdateException(FILE_ID_EMPTY.format());
        }
        if (isBlank(permissionId)) {
            throw new PermissionUpdateException(PERMISSION_ID_EMPTY.format());
        }
        this.permissionId = permissionId;
        this.fileId = fileId;
        this.role = role;
    }

    @Override
    public Permission execute(Drive drive) throws IOException {
        Permission updatedPermission = new Permission();
        role.set(updatedPermission);

        Drive.Permissions.Update update = drive.permissions()
                .update(fileId, permissionId, updatedPermission);

        if (PermissionRole.OWNER.equals(role)) {
            // This flag is mandatory when assigning an 'Owner' permission role.
            update.setTransferOwnership(true);
        }

        return update
                .setFields(join(",", PermissionUpdateAttribute.ALL_ATTRIBUTES))
                .execute();
    }

    @Override
    public PlatformException onException(Exception exception) {
        String error = GENERIC_ERROR.format(
                fileId,
                role,
                exception.getMessage());
        return new PermissionUpdateException(error, exception);
    }
}
