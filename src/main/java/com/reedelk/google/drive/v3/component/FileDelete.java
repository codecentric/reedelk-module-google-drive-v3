package com.reedelk.google.drive.v3.component;

import com.reedelk.google.drive.v3.internal.DriveApi;
import com.reedelk.google.drive.v3.internal.DriveApiFactory;
import com.reedelk.google.drive.v3.internal.attribute.FileDeleteAttributes;
import com.reedelk.google.drive.v3.internal.command.FileDeleteCommand;
import com.reedelk.google.drive.v3.internal.exception.FileDeleteException;
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

import static com.reedelk.google.drive.v3.internal.commons.Messages.FileDelete.FILE_ID_NULL;
import static com.reedelk.runtime.api.commons.ComponentPrecondition.Input;
import static com.reedelk.runtime.api.commons.DynamicValueUtils.isNullOrBlank;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ModuleComponent("Drive File Delete")
@Component(service = FileDelete.class, scope = PROTOTYPE)
@ComponentOutput(
        attributes = FileDeleteAttributes.class,
        payload = String.class,
        description = "The ID of the deleted file on Google Drive.")
@Description("Deletes a file with the given file ID from Google Drive. " +
        "The ID of the file to be deleted is taken from the input message payload if not defined in the 'File ID' property. " +
        "An expression can be used to dynamically evaluate the file ID. " +
        "This component requires the configuration of a Service Account to make authorized API calls " +
        "on behalf of the user. The component's configuration uses the private key (in JSON format) " +
        "of the Google Service Account which can be generated and downloaded from the Service Account page. " +
        "More info about Service Accounts and how they can be created and configured can " +
        "be found in the official Google Service Accounts <a href=\"https://cloud.google.com/iam/docs/service-accounts\">Documentation</a> page.")
public class FileDelete implements ProcessorSync {

    @Property("Configuration")
    @Description("The Google Service Account Configuration to be used to connect to Google Drive." +
            "This component requires the configuration of a Service Account to make authorized API calls " +
            "on behalf of the user. More info about Service Accounts and how they can be configured can " +
            "be found at the following <a href=\"https://cloud.google.com/iam/docs/service-accounts\">link</a>.")
    private DriveConfiguration configuration;

    @Property("File ID")
    @Hint("1f1Vx-AanOdkVEQoewRhUQibOiyXq_RHG")
    @Example("1f1Vx-AanOdkVEQoewRhUQibOiyXq_RHG")
    @Description("The ID of the file to delete. If not defined, the file ID is taken from the message payload.")
    private DynamicString fileId;

    @Reference
    ScriptEngineService scriptEngine;
    @Reference
    ConverterService converterService;

    DriveApi driveApi;

    @Override
    public void initialize() {
        driveApi = DriveApiFactory.create(FileDelete.class, configuration);
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {

        String realFileId;
        if (isNullOrBlank(fileId)) {
            // We take it from the message payload. The payload might not be a string,
            // for example when we upload the File ID from a rest listener and we forget
            // the mime type, therefore we have to convert it to a string type.
            Object payload = message.payload();

            Input.requireTypeMatchesAny(FileDelete.class, payload, String.class, byte[].class);

            realFileId = converterService.convert(payload, String.class);

        } else {
            realFileId = scriptEngine.evaluate(fileId, flowContext, message)
                    .orElseThrow(() -> new FileDeleteException(FILE_ID_NULL.format(fileId.value())));
        }

        FileDeleteCommand command = new FileDeleteCommand(realFileId);

        driveApi.execute(command);

        FileDeleteAttributes attributes = new FileDeleteAttributes(realFileId);

        return MessageBuilder.get(FileDelete.class)
                .withString(realFileId, MimeType.TEXT_PLAIN)
                .attributes(attributes)
                .build();
    }

    public void setConfiguration(DriveConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setFileId(DynamicString fileId) {
        this.fileId = fileId;
    }
}
