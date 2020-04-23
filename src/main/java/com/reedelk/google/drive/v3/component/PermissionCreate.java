package com.reedelk.google.drive.v3.component;

import com.google.api.services.drive.model.Permission;
import com.reedelk.google.drive.v3.internal.DriveApi;
import com.reedelk.google.drive.v3.internal.DriveApiFactory;
import com.reedelk.google.drive.v3.internal.attribute.PermissionCreateAttribute;
import com.reedelk.google.drive.v3.internal.command.PermissionCreateCommand;
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

import java.util.Optional;

import static com.reedelk.google.drive.v3.internal.commons.Default.SEND_NOTIFICATION_EMAIL;
import static com.reedelk.google.drive.v3.internal.commons.PermissionUtils.checkPreconditions;
import static com.reedelk.runtime.api.commons.DynamicValueUtils.isNullOrBlank;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ModuleComponent("Drive Permission Create")
@Component(service = PermissionCreate.class, scope = PROTOTYPE)
public class PermissionCreate implements ProcessorSync {

    @Property("Configuration")
    @Description("The Google Service Account Configuration to be used to connect to Google Drive." +
            "This component requires the configuration of a Service Account to make authorized API calls " +
            "on behalf of the user. More info about Service Accounts and how they can be configured can " +
            "be found at the following <a href=\"https://cloud.google.com/iam/docs/service-accounts\">link</a>.")
    private DriveConfiguration configuration;

    @Property("File ID")
    @Description("The ID of the file or shared drive we want to create this permission for. " +
            "If empty, the file ID is taken from the message payload.")
    private DynamicString fileId;

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

    @Property("Send Notification Email")
    @Group("Advanced")
    @InitValue("true")
    private Boolean sendNotificationEmail;

    @Reference
    private ScriptEngineService scriptEngine;
    @Reference
    private ConverterService converterService;

    private DriveApi driveApi;

    private Boolean realSendNotificationEmail;

    @Override
    public void initialize() {
        driveApi = DriveApiFactory.create(PermissionCreate.class, configuration);
        checkPreconditions(role, type, emailAddress, domain);
        this.realSendNotificationEmail = Optional.ofNullable(sendNotificationEmail).orElse(SEND_NOTIFICATION_EMAIL);
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

        String evaluatedEmailAddress = scriptEngine.evaluate(emailAddress, flowContext, message).orElse(null);
        String evaluatedDomain = scriptEngine.evaluate(domain, flowContext, message).orElse(null);

        PermissionCreateCommand command =
                new PermissionCreateCommand(realFileId, role, type, evaluatedEmailAddress, evaluatedDomain, realSendNotificationEmail);

        Permission created = driveApi.execute(command);

        String permissionId = created.getId();

        PermissionCreateAttribute attribute = new PermissionCreateAttribute(created.getId(), realFileId);

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

    public void setRole(PermissionRole role) {
        this.role = role;
    }

    public void setType(PermissionType type) {
        this.type = type;
    }
}
