package com.reedelk.google.drive.v3.component;

import com.reedelk.google.drive.v3.internal.DriveApi;
import com.reedelk.google.drive.v3.internal.DriveApiFactory;
import com.reedelk.google.drive.v3.internal.attribute.FileUpdateAttributes;
import com.reedelk.google.drive.v3.internal.command.FileUpdateCommand;
import com.reedelk.google.drive.v3.internal.exception.FileUpdateException;
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

import static com.reedelk.google.drive.v3.internal.commons.Messages.FileUpdate.FILE_ID_NULL;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ModuleComponent("Drive File Update")
@Component(service = FileUpdate.class, scope = PROTOTYPE)
@ComponentInput(
        payload = { String.class, byte[].class },
        description = "The updated content of the file to update on Google Drive.")
@ComponentOutput(
        attributes = FileUpdateAttributes.class,
        payload = String.class,
        description = "The ID of the updated file on Google Drive.")
@Description("Updates the content of the file with the given file ID in Google Drive. " +
        "The 'File ID' property is mandatory and a dynamic expression can be used to dynamically evaluate the file ID. " +
        "The updated content of the file to update is taken from the input message payload and if the file was successfully created " +
        "the ID of the file is returned in the output message payload. " +
        "This component requires the configuration of a Service Account to make authorized API calls " +
        "on behalf of the user. The component's configuration uses the private key (in JSON format) " +
        "of the Google Service Account which can be generated and downloaded from the Service Account page. " +
        "More info about Service Accounts and how they can be created and configured can " +
        "be found in the official Google Service Accounts <a href=\"https://cloud.google.com/iam/docs/service-accounts\">Documentation</a> page.")
public class FileUpdate implements ProcessorSync {

    @DialogTitle("Service Account Configuration")
    @Property("Configuration")
    @Description("The Google Service Account Configuration to be used to connect to Google Drive." +
            "This component requires the configuration of a Service Account to make authorized API calls " +
            "on behalf of the user. More info about Service Accounts and how they can be configured can " +
            "be found at the following <a href=\"https://cloud.google.com/iam/docs/service-accounts\">link</a>.")
    private DriveConfiguration configuration;

    @Property("File ID")
    @Hint("1f1Vx-AanOdkVEQoewRhUQibOiyXq_RHG")
    @Example("1f1Vx-AanOdkVEQoewRhUQibOiyXq_RHG")
    @Description("The ID of the file we want to update. The file ID is mandatory.")
    private DynamicString fileId;

    @Reference
    ScriptEngineService scriptEngine;
    @Reference
    ConverterService converterService;

    DriveApi driveApi;

    @Override
    public void initialize() {
        driveApi = DriveApiFactory.create(FileUpdate.class, configuration);
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {

        String realFileId = scriptEngine.evaluate(fileId, flowContext, message)
                .orElseThrow(() -> new FileUpdateException(FILE_ID_NULL.format(fileId.value())));

        Object payload = message.payload();

        byte[] fileContent = converterService.convert(payload, byte[].class);

        FileUpdateCommand command = new FileUpdateCommand(realFileId, fileContent);

        driveApi.execute(command);

        FileUpdateAttributes attributes = new FileUpdateAttributes(realFileId);

        return MessageBuilder.get(FileUpdate.class)
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
