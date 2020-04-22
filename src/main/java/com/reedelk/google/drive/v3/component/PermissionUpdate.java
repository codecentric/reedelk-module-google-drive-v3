package com.reedelk.google.drive.v3.component;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;
import com.reedelk.google.drive.v3.internal.DriveService;
import com.reedelk.google.drive.v3.internal.commons.PermissionUtils;
import com.reedelk.google.drive.v3.internal.exception.PermissionDeleteException;
import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.converter.ConverterService;
import com.reedelk.runtime.api.exception.PlatformException;
import com.reedelk.runtime.api.flow.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.script.ScriptEngineService;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.IOException;

import static com.reedelk.google.drive.v3.internal.commons.PermissionUtils.createPermission;
import static com.reedelk.runtime.api.commons.DynamicValueUtils.isNullOrBlank;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ModuleComponent("Google Permission Update")
@Component(service = PermissionUpdate.class, scope = PROTOTYPE)
public class PermissionUpdate implements ProcessorSync {

    @Property("Configuration")
    @Description("The Google Drive Configuration providing the Service Account Credentials file.")
    private DriveConfiguration configuration;

    @Property("File ID")
    @Description("The ID of the file we want to list the permission. " +
            "If empty, the file ID is taken from the message payload.")
    private DynamicString fileId;

    @Property("Permission ID")
    @Description("The ID of the permission we want to delete. " +
            "If empty, the permission ID is taken from the message payload.")
    private DynamicString permissionId;

    @Property("Role")
    @Example("OWNER")
    @InitValue("READER")
    @DefaultValue("READER")
    @Description("The role granted by this permission.")
    private PermissionRole role = PermissionRole.READER;

    @Property("Type")
    @InitValue("USER")
    @Example("GROUP")
    @DefaultValue("USER")
    @Description("The type of the grantee. When creating a permission, if type is user or group, " +
            "you must provide an emailAddress for the user or group. " +
            "When type is domain, you must provide a domain. " +
            "There isn't extra information required for a anyone type.")
    private PermissionType type = PermissionType.USER;

    @Property("Email")
    @Description("The email address of the user or group to which this permission refers.")
    @When(propertyName = "type", propertyValue = "USER")
    @When(propertyName = "type", propertyValue = "GROUP")
    @When(propertyName = "type", propertyValue = When.NULL)
    private DynamicString emailAddress;

    @Property("Domain")
    @Description("The domain to which this permission refers.")
    @When(propertyName = "type", propertyValue = "DOMAIN")
    private DynamicString domain;

    private Drive drive;

    @Reference
    private ScriptEngineService scriptEngine;
    @Reference
    private ConverterService converterService;

    @Override
    public void initialize() {
        drive = DriveService.create(PermissionUpdate.class, configuration);
        PermissionUtils.checkPreconditions(role, type, emailAddress, domain);
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {
        Permission updatedPermission =
                createPermission(scriptEngine, emailAddress, domain, role, type, flowContext, message);

        String realFileId;
        if (isNullOrBlank(fileId)) {
            // We take it from the message payload.
            Object payload = message.payload(); // The payload might not be a string.
            realFileId = converterService.convert(payload, String.class);
        } else {
            realFileId = scriptEngine.evaluate(fileId, flowContext, message)
                    .orElseThrow(() -> new PlatformException("File ID must not be null."));
        }

        String realPermissionId;
        if (isNullOrBlank(permissionId)) {
            // We take it from the message payload.
            Object payload = message.payload(); // The payload might not be a string.
            realPermissionId = converterService.convert(payload, String.class);
        } else {
            realPermissionId = scriptEngine.evaluate(permissionId, flowContext, message)
                    .orElseThrow(() -> new PermissionDeleteException("Permission ID must not be null."));
        }

        Permission result;
        try {
            Drive.Permissions.Update update = drive.permissions()
                    .update(realFileId, realPermissionId, updatedPermission);

            if (PermissionRole.OWNER.equals(role)) {
                // This flag is mandatory when assigning an 'Owner' permission role.
                update.setTransferOwnership(true);
            }

            result = update.execute();

        } catch (IOException e) {
            throw new PlatformException(e);
        }

        String permissionId = result.getId();
        return MessageBuilder.get(PermissionCreate.class)
                .withString(permissionId, MimeType.TEXT_PLAIN)
                .build();
    }
}
