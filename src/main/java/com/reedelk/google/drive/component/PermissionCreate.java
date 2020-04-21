package com.reedelk.google.drive.component;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;
import com.reedelk.google.drive.internal.DriveServiceProvider;
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

import static com.reedelk.runtime.api.commons.DynamicValueUtils.isNullOrBlank;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ModuleComponent("Google Permission Create")
@Component(service = PermissionCreate.class, scope = PROTOTYPE)
public class PermissionCreate implements ProcessorSync {

    @Property("Configuration")
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

    private Drive drive;

    @Reference
    private ScriptEngineService scriptEngine;
    @Reference
    private ConverterService converterService;

    @Override
    public void initialize() {
        DriveServiceProvider provider = new DriveServiceProvider();
        this.drive = provider.get(FileCreate.class, configuration);

    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {
        Permission userPermission = createPermission(flowContext, message);

        String realFileId;
        if (isNullOrBlank(fileId)) {
            // We take it from the message payload.
            Object payload = message.payload(); // The payload might not be a string.
            realFileId = converterService.convert(payload, String.class);
        } else {
            realFileId = scriptEngine.evaluate(fileId, flowContext, message)
                    .orElseThrow(() -> new PlatformException("File ID must not be null."));
        }

        try {
            Permission result = drive.permissions()
                    .create(realFileId, userPermission)
                    .execute();

            String permissionId = result.getId();

            return MessageBuilder.get(PermissionCreate.class)
                    .withString(permissionId, MimeType.TEXT_PLAIN)
                    .build();

        } catch (IOException e) {
            throw new PlatformException(e);
        }
    }

    private Permission createPermission(FlowContext flowContext, Message message) {
        Permission userPermission = new Permission();
        role.set(userPermission);
        type.set(userPermission);

        // TODO: Add extra check on INIT.
        if (PermissionType.USER.equals(type) || PermissionType.GROUP.equals(type)) {
            // emailAddress is mandatory (see Google Doc: https://developers.google.com/drive/api/v3/reference/permissions/create).
            String evaluatedEmailAddress = scriptEngine.evaluate(emailAddress, flowContext, message)
                    .orElseThrow(() -> new PlatformException("Email address is mandatory when permission type is user or group"));
            userPermission.setEmailAddress(evaluatedEmailAddress);

        } else if (PermissionType.DOMAIN.equals(type)) {
            // domain is mandatory (see Google Doc: https://developers.google.com/drive/api/v3/reference/permissions/create).
            String evaluatedDomain = scriptEngine.evaluate(domain, flowContext, message)
                    .orElseThrow(() -> new PlatformException("Domain is mandatory when permission type is user or group"));
            userPermission.setDomain(evaluatedDomain);
        }
        return userPermission;
    }

    public void setConfiguration(DriveConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setFileId(DynamicString fileId) {
        this.fileId = fileId;
    }

    public void setRole(PermissionRole role) {
        this.role = role;
    }

    public void setType(PermissionType type) {
        this.type = type;
    }

    public void setEmailAddress(DynamicString emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void setDomain(DynamicString domain) {
        this.domain = domain;
    }
}
