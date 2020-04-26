package com.reedelk.google.drive.v3.component;

import com.google.api.services.drive.model.Permission;
import com.reedelk.google.drive.v3.internal.DriveApi;
import com.reedelk.google.drive.v3.internal.DriveApiFactory;
import com.reedelk.google.drive.v3.internal.attribute.PermissionCreateAttribute;
import com.reedelk.google.drive.v3.internal.command.PermissionCreateCommand;
import com.reedelk.google.drive.v3.internal.exception.PermissionCreateException;
import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.converter.ConverterService;
import com.reedelk.runtime.api.flow.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.script.ScriptEngineService;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Optional;

import static com.reedelk.google.drive.v3.internal.commons.Default.SEND_NOTIFICATION_EMAIL;
import static com.reedelk.google.drive.v3.internal.commons.Messages.PermissionCreate.FILE_ID_NULL;
import static com.reedelk.google.drive.v3.internal.commons.PermissionUtils.checkPreconditions;
import static com.reedelk.runtime.api.commons.ComponentPrecondition.Input;
import static com.reedelk.runtime.api.commons.DynamicValueUtils.isNullOrBlank;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ModuleComponent("Drive Permission Create")
@Component(service = PermissionCreate.class, scope = PROTOTYPE)
@Description("Creates a new permission for a file in Google Drive. " +
        "If not defined in the 'File ID' property, the ID of the file we want to assign the permission to is taken from the input message payload. " +
        "A permission grants a user, group, domain or the world access to a file or a folder hierarchy." +
        "This component requires the configuration of a Service Account to make authorized API calls " +
        "on behalf of the user. The component's configuration uses the private key (in JSON format) " +
        "of the Google Service Account which can be generated and downloaded from the Service Account page. " +
        "More info about Service Accounts and how they can be created and configured can " +
        "be found in the official Google Service Accounts <a href=\"https://cloud.google.com/iam/docs/service-accounts\">Documentation</a> page.")
public class PermissionCreate implements ProcessorSync {

    @Property("Configuration")
    @Description("The Google Service Account Configuration to be used to connect to Google Drive." +
            "This component requires the configuration of a Service Account to make authorized API calls " +
            "on behalf of the user. More info about Service Accounts and how they can be configured can " +
            "be found at the following <a href=\"https://cloud.google.com/iam/docs/service-accounts\">link</a>.")
    private DriveConfiguration configuration;

    @Property("Permission File ID")
    @DefaultValue("#[]")
    @Hint("1f1Vx-AanOdkVEQoewRhUQibOiyXq_RHG")
    @Example("1f1Vx-AanOdkVEQoewRhUQibOiyXq_RHG")
    @Description("The ID of the file or shared drive we want to create this permission for. " +
            "If empty, the file ID is taken from the message payload.")
    private DynamicString fileId;

    @Property("Permission Type")
    @InitValue("USER")
    @Example("GROUP")
    @DefaultValue("USER")
    @Description("The type of the grantee. When creating a permission, if type is user or group, " +
            "you must provide an emailAddress for the user or group. " +
            "When type is domain, you must provide a domain. " +
            "There isn't extra information required for a anyone type.")
    private PermissionType type = PermissionType.USER;

    @Property("Permission Role")
    @Example("OWNER")
    @InitValue("READER")
    @DefaultValue("READER")
    @Description("The role granted by this permission.")
    private PermissionRole role = PermissionRole.READER;

    @Property("Email")
    @Hint("my-user@mydomain.com")
    @Example("my-user@mydomain.com")
    @Description("The email address of the user or group to which this permission refers.")
    @When(propertyName = "type", propertyValue = "USER")
    @When(propertyName = "type", propertyValue = "GROUP")
    @When(propertyName = "type", propertyValue = When.NULL)
    private DynamicString emailAddress;

    @Property("Domain")
    @Hint("www.mydomain.com")
    @Example("www.mydomain.com")
    @Description("The domain to which this permission refers.")
    @When(propertyName = "type", propertyValue = "DOMAIN")
    private DynamicString domain;

    @Property("Send Notification Email")
    @Group("Advanced")
    @InitValue("true")
    private Boolean sendNotificationEmail;

    @Reference
    ScriptEngineService scriptEngine;
    @Reference
    ConverterService converterService;

    DriveApi driveApi;

    private Boolean realSendNotificationEmail;

    @Override
    public void initialize() {
        checkPreconditions(type, emailAddress, domain);
        driveApi = createApi();
        realSendNotificationEmail =
                Optional.ofNullable(sendNotificationEmail).orElse(SEND_NOTIFICATION_EMAIL);
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {

        String realFileId;
        if (isNullOrBlank(fileId)) {
            // We take it from the message payload. The payload might not be a string,
            // for example when we upload the File ID from a rest listener and we forget
            // the mime type, therefore we have to convert it to a string type.
            Object payload = message.payload(); // The payload might not be a string.

            Input.requireTypeMatchesAny(PermissionCreate.class, payload, String.class, byte[].class);

            realFileId = converterService.convert(payload, String.class);

        } else {
            realFileId = scriptEngine.evaluate(fileId, flowContext, message)
                    .orElseThrow(() -> new PermissionCreateException(FILE_ID_NULL.format(fileId.value())));
        }

        String evaluatedEmailAddress = scriptEngine.evaluate(emailAddress, flowContext, message).orElse(null);
        String evaluatedDomain = scriptEngine.evaluate(domain, flowContext, message).orElse(null);

        PermissionCreateCommand command =
                new PermissionCreateCommand(realFileId, role, type, evaluatedEmailAddress, evaluatedDomain, realSendNotificationEmail);

        Permission created = driveApi.execute(command);

        String permissionId = created.getId();

        PermissionCreateAttribute attribute = new PermissionCreateAttribute(realFileId, created);

        return MessageBuilder.get(PermissionCreate.class)
                .withString(permissionId, MimeType.TEXT_PLAIN)
                .attributes(attribute)
                .build();
    }

    public void setConfiguration(DriveConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setEmailAddress(DynamicString emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void setFileId(DynamicString fileId) {
        this.fileId = fileId;
    }

    public void setDomain(DynamicString domain) {
        this.domain = domain;
    }

    public void setSendNotificationEmail(Boolean sendNotificationEmail) {
        this.sendNotificationEmail = sendNotificationEmail;
    }

    public void setRole(PermissionRole role) {
        this.role = role;
    }

    public void setType(PermissionType type) {
        this.type = type;
    }

    DriveApi createApi() {
        return DriveApiFactory.create(PermissionCreate.class, configuration);
    }
}
