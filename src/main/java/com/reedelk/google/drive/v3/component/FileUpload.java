package com.reedelk.google.drive.v3.component;

import com.google.api.services.drive.model.File;
import com.reedelk.google.drive.v3.internal.DriveApi;
import com.reedelk.google.drive.v3.internal.DriveApiFactory;
import com.reedelk.google.drive.v3.internal.attribute.FileUploadAttributes;
import com.reedelk.google.drive.v3.internal.command.FileUploadCommand;
import com.reedelk.google.drive.v3.internal.exception.FileUploadException;
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

import static com.reedelk.google.drive.v3.internal.commons.Default.CONTENT_AS_INDEXABLE_TEXT;
import static com.reedelk.runtime.api.commons.ComponentPrecondition.Configuration.requireNotNull;
import static java.util.Optional.ofNullable;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ModuleComponent("Drive File Upload")
@Component(service = FileUpload.class, scope = PROTOTYPE)
@Description("Uploads a new file in Google Drive with the given name and optional description. " +
        "The content of the file is taken from the input message payload and if the file was successfully created " +
        "the ID of the file is returned in the output message payload. The default file owner will be the provided Service Account and permissions " +
        "on the file must be used in order to assign further read/write permissions to other users. New permissions can be " +
        "created using the 'Drive Permission Create' component. " +
        "This component requires the configuration of a Service Account to make authorized API calls " +
        "on behalf of the user. The component's configuration uses the private key (in JSON format) " +
        "of the Google Service Account which can be generated and downloaded from the Service Account page. " +
        "More info about Service Accounts and how they can be created and configured can " +
        "be found in the official Google Service Accounts <a href=\"https://cloud.google.com/iam/docs/service-accounts\">Documentation</a> page.")
public class FileUpload implements ProcessorSync {

    @Property("Configuration")
    @Description("The Google Service Account Configuration to be used to connect to Google Drive." +
            "This component requires the configuration of a Service Account to make authorized API calls " +
            "on behalf of the user. More info about Service Accounts and how they can be created and configured can " +
            "be found at the following <a href=\"https://cloud.google.com/iam/docs/service-accounts\">link</a>.")
    private DriveConfiguration configuration;

    @Property("File name")
    @Hint("my_picture.jpg")
    @Example("my_picture.jpg")
    @Description("The name of the file. This is not necessarily unique within a folder. " +
            "Note that for immutable items such as the top level folders of shared drives, " +
            "My Drive root folder, and Application Data folder the name is constant.")
    private DynamicString fileName;

    @Property("File description")
    @Hint("My document")
    @Example("My document")
    @Description("A short description of the file.")
    private DynamicString fileDescription;

    @Property("Parent Folder ID")
    @Hint("0BwwA4oUTeiV1TGRPeTVjaWRDY1E")
    @Example("0BwwA4oUTeiV1TGRPeTVjaWRDY1E")
    @Description("The ID of the parent folder which contain the file.\n" +
            "If not specified, the file will be placed directly in the service account's My Drive folder. ")
    private DynamicString parentFolderId;

    @Property("Use content as indexable text")
    @DefaultValue("false")
    @Group("Advanced")
    @Example("true")
    @Description("Whether to use the uploaded content as indexable text.")
    private Boolean indexableText;

    @Reference
    private ScriptEngineService scriptEngine;
    @Reference
    private ConverterService converterService;

    private DriveApi driveApi;

    private boolean realIndexableText;

    @Override
    public void initialize() {
        requireNotNull(FileUpload.class, fileName, "Google Drive File name must not be empty.");
        driveApi = DriveApiFactory.create(FileUpload.class, configuration);
        realIndexableText = ofNullable(indexableText).orElse(CONTENT_AS_INDEXABLE_TEXT);
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {

        String finalFileName = scriptEngine.evaluate(fileName, flowContext, message)
                .orElseThrow(() -> new FileUploadException("File name must not be empty"));

        String finalFileDescription = scriptEngine.evaluate(fileDescription, flowContext, message)
                .orElse(null); // Not mandatory.

        String finalParentFolderId = scriptEngine.evaluate(parentFolderId, flowContext, message)
                .orElse(null); // Not mandatory.

        Object payload = message.payload();

        byte[] fileContent = converterService.convert(payload, byte[].class);

        FileUploadCommand command =
                new FileUploadCommand(finalFileName, finalFileDescription, finalParentFolderId, realIndexableText, fileContent);

        File file = driveApi.execute(command);

        FileUploadAttributes attributes = new FileUploadAttributes(file);

        return MessageBuilder.get(FileUpload.class)
                .withString(file.getId(), MimeType.TEXT_PLAIN)
                .attributes(attributes)
                .build();
    }

    public void setFileDescription(DynamicString fileDescription) {
        this.fileDescription = fileDescription;
    }

    public void setConfiguration(DriveConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setParentFolderId(DynamicString parentFolderId) {
        this.parentFolderId = parentFolderId;
    }

    public void setIndexableText(Boolean indexableText) {
        this.indexableText = indexableText;
    }

    public void setFileName(DynamicString fileName) {
        this.fileName = fileName;
    }
}
