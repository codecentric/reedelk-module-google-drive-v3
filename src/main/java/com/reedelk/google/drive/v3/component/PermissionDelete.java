package com.reedelk.google.drive.v3.component;

import com.reedelk.google.drive.v3.internal.DriveApi;
import com.reedelk.google.drive.v3.internal.DriveApiFactory;
import com.reedelk.google.drive.v3.internal.attribute.PermissionDeleteAttribute;
import com.reedelk.google.drive.v3.internal.command.PermissionDeleteCommand;
import com.reedelk.google.drive.v3.internal.exception.PermissionDeleteException;
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

import static com.reedelk.google.drive.v3.internal.commons.Messages.PermissionDelete.FILE_ID_NULL;
import static com.reedelk.google.drive.v3.internal.commons.Messages.PermissionDelete.PERMISSION_ID_NULL;
import static com.reedelk.runtime.api.commons.ComponentPrecondition.Input;
import static com.reedelk.runtime.api.commons.DynamicValueUtils.isNullOrBlank;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ModuleComponent("Drive Permission Delete")
@Component(service = PermissionDelete.class, scope = PROTOTYPE)
@Description("Deletes a permission from a file in Google Drive. " +
        "If not defined in the 'Permission ID' property, the ID of the permission we want to remove is taken from the input message payload. " +
        "This component requires to specify a not empty 'File ID' property which identifies the file we want to remove the permission from. " +
        "This component requires the configuration of a Service Account to make authorized API calls " +
        "on behalf of the user. The component's configuration uses the private key (in JSON format) " +
        "of the Google Service Account which can be generated and downloaded from the Service Account page. " +
        "More info about Service Accounts and how they can be created and configured can " +
        "be found in the official Google Service Accounts <a href=\"https://cloud.google.com/iam/docs/service-accounts\">Documentation</a> page.")
public class PermissionDelete implements ProcessorSync {

    @Property("Configuration")
    @Description("The Google Service Account Configuration to be used to connect to Google Drive." +
            "This component requires the configuration of a Service Account to make authorized API calls " +
            "on behalf of the user. More info about Service Accounts and how they can be configured can " +
            "be found at the following <a href=\"https://cloud.google.com/iam/docs/service-accounts\">link</a>.")
    private DriveConfiguration configuration;

    @Property("Permission File ID")
    @Hint("1f1Vx-AanOdkVEQoewRhUQibOiyXq_RHG")
    @Example("1f1Vx-AanOdkVEQoewRhUQibOiyXq_RHG")
    @Description("The ID of the file we want to change the permission.")
    private DynamicString fileId;

    @Property("Permission ID")
    @Hint("13346476095080557008")
    @Example("13346476095080557008")
    @Description("The ID of the permission we want to delete. " +
            "If empty, the permission ID is taken from the message payload.")
    private DynamicString permissionId;

    @Reference
    ScriptEngineService scriptEngine;
    @Reference
    ConverterService converterService;

    DriveApi driveApi;

    @Override
    public void initialize() {
        driveApi = DriveApiFactory.create(PermissionDelete.class, configuration);
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {

        String realPermissionId;
        if (isNullOrBlank(permissionId)) {
            // We take it from the message payload. The payload might not be a string,
            // for example when we upload the Permission ID from a rest listener and we forget
            // the mime type, therefore we have to convert it to a string type.
            Object payload = message.payload(); // The payload might not be a string.

            Input.requireTypeMatchesAny(PermissionDelete.class, payload, String.class, byte[].class);

            realPermissionId = converterService.convert(payload, String.class);

        } else {
            realPermissionId = scriptEngine.evaluate(permissionId, flowContext, message)
                    .orElseThrow(() -> new PermissionDeleteException(PERMISSION_ID_NULL.format(permissionId.value())));
        }

        String realFileId = scriptEngine.evaluate(fileId, flowContext, message)
                .orElseThrow(() -> new PermissionDeleteException(FILE_ID_NULL.format(fileId.value())));

        PermissionDeleteCommand command = new PermissionDeleteCommand(realPermissionId, realFileId);

        driveApi.execute(command);

        PermissionDeleteAttribute attribute = new PermissionDeleteAttribute(realPermissionId, realFileId);

        return MessageBuilder.get(PermissionDelete.class)
                .withString(realPermissionId, MimeType.TEXT_PLAIN)
                .attributes(attribute)
                .build();
    }

    public void setConfiguration(DriveConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setPermissionId(DynamicString permissionId) {
        this.permissionId = permissionId;
    }

    public void setFileId(DynamicString fileId) {
        this.fileId = fileId;
    }
}
