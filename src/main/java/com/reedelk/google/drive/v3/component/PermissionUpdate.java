package com.reedelk.google.drive.v3.component;

import com.google.api.services.drive.model.Permission;
import com.reedelk.google.drive.v3.internal.DriveApi;
import com.reedelk.google.drive.v3.internal.DriveApiFactory;
import com.reedelk.google.drive.v3.internal.attribute.PermissionUpdateAttribute;
import com.reedelk.google.drive.v3.internal.command.PermissionUpdateCommand;
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

import static com.reedelk.runtime.api.commons.DynamicValueUtils.isNullOrBlank;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ModuleComponent("Drive Permission Update")
@Component(service = PermissionUpdate.class, scope = PROTOTYPE)
@Description("Updates a permissions for a given file in Google Drive. " +
        "If not defined in the 'Permission ID' property, the ID of the permission we want to update is taken from the input message payload. " +
        "This component requires to specify a not empty 'File ID' property which identifies the file we want to update the permission to. " +
        "This component requires the configuration of a Service Account to make authorized API calls " +
        "on behalf of the user. The component's configuration uses the private key (in JSON format) " +
        "of the Google Service Account which can be generated and downloaded from the Service Account page. " +
        "More info about Service Accounts and how they can be created and configured can " +
        "be found in the official Google Service Accounts <a href=\"https://cloud.google.com/iam/docs/service-accounts\">Documentation</a> page.")
public class PermissionUpdate implements ProcessorSync {

    @Property("Configuration")
    @Description("The Google Service Account Configuration to be used to connect to Google Drive." +
            "This component requires the configuration of a Service Account to make authorized API calls " +
            "on behalf of the user. More info about Service Accounts and how they can be configured can " +
            "be found at the following <a href=\"https://cloud.google.com/iam/docs/service-accounts\">link</a>.")
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

    @Reference
    private ScriptEngineService scriptEngine;
    @Reference
    private ConverterService converterService;

    private DriveApi driveApi;

    @Override
    public void initialize() {
        driveApi = DriveApiFactory.create(PermissionUpdate.class, configuration);
        PermissionUtils.checkPreconditions(role, type, emailAddress, domain);
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {

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

        String evaluatedEmailAddress = scriptEngine.evaluate(emailAddress, flowContext, message).orElse(null);
        String evaluatedDomain = scriptEngine.evaluate(domain, flowContext, message).orElse(null);

        PermissionUpdateCommand command =
                new PermissionUpdateCommand(realFileId, realPermissionId, role, type, evaluatedEmailAddress, evaluatedDomain);

        Permission updated = driveApi.execute(command);

        String permissionId = updated.getId();

        PermissionUpdateAttribute attribute = new PermissionUpdateAttribute(permissionId, realFileId);

        return MessageBuilder.get(PermissionCreate.class)
                .withString(permissionId, MimeType.TEXT_PLAIN)
                .attributes(attribute)
                .build();
    }
}
