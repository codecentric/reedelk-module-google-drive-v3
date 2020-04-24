package com.reedelk.google.drive.v3.internal.command;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;
import com.reedelk.google.drive.v3.component.PermissionRole;
import com.reedelk.google.drive.v3.component.PermissionType;
import com.reedelk.google.drive.v3.internal.exception.PermissionCreateException;
import com.reedelk.runtime.api.commons.StringUtils;
import com.reedelk.runtime.api.exception.PlatformException;

import java.io.IOException;

import static com.reedelk.runtime.api.commons.Preconditions.checkState;

public class PermissionCreateCommand implements Command<Permission> {

    private final boolean sendNotificationEmail;
    private final PermissionRole role;
    private final PermissionType type;
    private final String emailAddress;
    private final String domain;
    private final String fileId;

    public PermissionCreateCommand(String fileId,
                                   PermissionRole role,
                                   PermissionType type,
                                   String emailAddress,
                                   String domain,
                                   boolean sendNotificationEmail) {
        this.sendNotificationEmail = sendNotificationEmail;
        this.emailAddress = emailAddress;
        this.domain = domain;
        this.fileId = fileId;
        this.role = role;
        this.type = type;
    }

    @Override
    public Permission execute(Drive drive) throws IOException {
        Permission userPermission = new Permission();
        role.set(userPermission);
        type.set(userPermission);

        Drive.Permissions.Create create = drive
                .permissions()
                .create(fileId, userPermission);

        if (PermissionType.USER.equals(type) || PermissionType.GROUP.equals(type)) {
            // emailAddress is mandatory (see Google Doc: https://developers.google.com/drive/api/v3/reference/permissions/create).
            checkState(StringUtils.isNotBlank(emailAddress),
                    "Email address is mandatory when permission type is user or group.");
            userPermission.setEmailAddress(emailAddress);
            create.setSendNotificationEmail(sendNotificationEmail);

        } else if (PermissionType.DOMAIN.equals(type)) {
            // domain is mandatory (see Google Doc: https://developers.google.com/drive/api/v3/reference/permissions/create).
            checkState(StringUtils.isNotBlank(domain),
                    "Domain is mandatory when permission type is domain.");
            userPermission.setDomain(domain);
        }

        if (PermissionRole.OWNER.equals(role)) {
            // This flag is mandatory when assigning an 'Owner' permission role.
            create.setTransferOwnership(true);
        }

        return create.execute();
    }

    @Override
    public PlatformException onException(Exception exception) {
        String error = "";
        return new PermissionCreateException(error, exception);
    }
}
