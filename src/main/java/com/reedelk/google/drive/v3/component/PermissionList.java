package com.reedelk.google.drive.v3.component;

import com.reedelk.google.drive.v3.internal.DriveApi;
import com.reedelk.google.drive.v3.internal.DriveApiFactory;
import com.reedelk.google.drive.v3.internal.attribute.PermissionListAttribute;
import com.reedelk.google.drive.v3.internal.command.PermissionListCommand;
import com.reedelk.google.drive.v3.internal.exception.PermissionListException;
import com.reedelk.google.drive.v3.internal.type.ListOfPermissions;
import com.reedelk.google.drive.v3.internal.type.Permission;
import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.converter.ConverterService;
import com.reedelk.runtime.api.flow.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.script.ScriptEngineService;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import static com.reedelk.google.drive.v3.internal.commons.Messages.PermissionList.FILE_ID_NULL;
import static com.reedelk.runtime.api.commons.ComponentPrecondition.Input;
import static com.reedelk.runtime.api.commons.DynamicValueUtils.isNullOrBlank;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ModuleComponent("Drive Permissions List")
@Component(service = PermissionList.class, scope = PROTOTYPE)
@ComponentOutput(
        attributes = PermissionListAttribute.class,
        payload = ListOfPermissions.class,
        description = "The list of permission for the file on Google Drive.")
@ComponentInput(
        payload = { String.class },
        description = "The input payload is used as file id for which the permissions should listed from.")
@Description("Lists all the permissions of a file in Google Drive. " +
        "If not defined in the 'File ID' property, the ID of the file we want to list the permissions from is taken from the input message payload. " +
        "This component requires the configuration of a Service Account to make authorized API calls " +
        "on behalf of the user. The component's configuration uses the private key (in JSON format) " +
        "of the Google Service Account which can be generated and downloaded from the Service Account page. " +
        "More info about Service Accounts and how they can be created and configured can " +
        "be found in the official Google Service Accounts <a href=\"https://cloud.google.com/iam/docs/service-accounts\">Documentation</a> page.")
public class PermissionList implements ProcessorSync {

    @DialogTitle("Service Account Configuration")
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
    @Description("The ID of the file we want to list the permission. " +
            "If empty, the file ID is taken from the message payload.")
    private DynamicString fileId;

    @Reference
    ScriptEngineService scriptEngine;
    @Reference
    ConverterService converterService;

    DriveApi driveApi;

    @Override
    public void initialize() {
        driveApi = DriveApiFactory.create(PermissionList.class, configuration);
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {

        String realFileId;
        if (isNullOrBlank(fileId)) {
            // We take it from the message payload. The payload might not be a string,
            // for example when we upload the File ID from a rest listener and we forget
            // the mime type, therefore we have to convert it to a string type.
            Object payload = message.payload(); // The payload might not be a string.

            Input.requireTypeMatchesAny(PermissionList.class, payload, String.class, byte[].class);

            realFileId = converterService.convert(payload, String.class);

        } else {
            realFileId = scriptEngine.evaluate(fileId, flowContext, message)
                    .orElseThrow(() -> new PermissionListException(FILE_ID_NULL.format(fileId.value())));
        }

        PermissionListCommand command = new PermissionListCommand(realFileId);

        ListOfPermissions permissions = driveApi.execute(command);

        PermissionListAttribute attribute = new PermissionListAttribute(realFileId);

        return MessageBuilder.get(PermissionList.class)
                .withList(permissions, Permission.class)
                .attributes(attribute)
                .build();
    }

    public void setConfiguration(DriveConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setFileId(DynamicString fileId) {
        this.fileId = fileId;
    }
}
