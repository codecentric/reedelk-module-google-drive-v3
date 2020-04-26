package com.reedelk.google.drive.v3.internal.command;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;
import com.reedelk.google.drive.v3.component.PermissionRole;
import com.reedelk.google.drive.v3.component.PermissionType;
import com.reedelk.google.drive.v3.internal.attribute.PermissionUpdateAttribute;
import com.reedelk.google.drive.v3.internal.exception.PermissionUpdateException;
import com.reedelk.runtime.api.exception.PlatformException;

import java.io.IOException;

import static com.reedelk.google.drive.v3.internal.commons.Messages.PermissionUpdate.*;
import static com.reedelk.runtime.api.commons.Preconditions.checkState;
import static com.reedelk.runtime.api.commons.StringUtils.isBlank;
import static com.reedelk.runtime.api.commons.StringUtils.isNotBlank;
import static java.lang.String.join;

public class PermissionUpdateCommand implements Command<Permission> {

    private final String fileId;
    private final String permissionId;
    private final PermissionRole role;
    private final PermissionType type;
    private final String emailAddress;
    private final String domain;

    public PermissionUpdateCommand(String fileId,
                                   String permissionId,
                                   PermissionRole role,
                                   PermissionType type,
                                   String emailAddress,
                                   String domain) {
        if (isBlank(fileId)) {
            throw new PermissionUpdateException(FILE_ID_EMPTY.format());
        }
        if (isBlank(permissionId)) {
            throw new PermissionUpdateException(PERMISSION_ID_EMPTY.format());
        }
        this.emailAddress = emailAddress;
        this.permissionId = permissionId;
        this.domain = domain;
        this.fileId = fileId;
        this.role = role;
        this.type = type;
    }

    @Override
    public Permission execute(Drive drive) throws IOException {
        Permission updatedPermission = new Permission();
        role.set(updatedPermission);
        type.set(updatedPermission);

        if (PermissionType.USER.equals(type) || PermissionType.GROUP.equals(type)) {
            // emailAddress is mandatory (see Google Doc: https://developers.google.com/drive/api/v3/reference/permissions/create).
            checkState(isNotBlank(emailAddress),
                    "Email address is mandatory when permission type is user or group.");
            updatedPermission.setEmailAddress(emailAddress);

        } else if (PermissionType.DOMAIN.equals(type)) {
            // domain is mandatory (see Google Doc: https://developers.google.com/drive/api/v3/reference/permissions/create).
            checkState(isNotBlank(emailAddress),
                    "Domain is mandatory when permission type is domain.");
            updatedPermission.setDomain(domain);
        }

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
                type,
                role,
                emailAddress,
                domain,
                exception.getMessage());
        return new PermissionUpdateException(error, exception);
    }
}
