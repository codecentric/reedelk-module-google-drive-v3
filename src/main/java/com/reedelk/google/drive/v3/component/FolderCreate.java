package com.reedelk.google.drive.v3.component;

import com.google.api.services.drive.model.File;
import com.reedelk.google.drive.v3.internal.DriveApi;
import com.reedelk.google.drive.v3.internal.DriveApiFactory;
import com.reedelk.google.drive.v3.internal.attribute.FolderCreateAttributes;
import com.reedelk.google.drive.v3.internal.command.FolderCreateCommand;
import com.reedelk.google.drive.v3.internal.exception.FolderCreateException;
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

import static com.reedelk.google.drive.v3.internal.commons.Messages.FolderCreate.FOLDER_NAME_NULL;
import static com.reedelk.runtime.api.commons.ComponentPrecondition.Input;
import static com.reedelk.runtime.api.commons.DynamicValueUtils.isNullOrBlank;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ModuleComponent("Drive Folder Create")
@Component(service = FolderCreate.class, scope = PROTOTYPE)
@ComponentOutput(
        attributes = FolderCreateAttributes.class,
        payload = String.class,
        description = "The ID of the new folder created on Google Drive.")
@Description("Creates a new folder in Google Drive with the given name and optional description. " +
        "The default folder owner will be the provided Service Account and permissions " +
        "on the folder must be used in order to assign further read/write permissions to other users. New permissions can be " +
        "created using the 'Drive Permission Create' component. " +
        "This component requires the configuration of a Service Account to make authorized API calls " +
        "on behalf of the user. The component's configuration uses the private key (in JSON format) " +
        "of the Google Service Account which can be generated and downloaded from the Service Account page. " +
        "More info about Service Accounts and how they can be created and configured can " +
        "be found in the official Google Service Accounts <a href=\"https://cloud.google.com/iam/docs/service-accounts\">Documentation</a> page.")
public class FolderCreate implements ProcessorSync {

    @Property("Configuration")
    @Description("The Google Service Account Configuration to be used to connect to Google Drive." +
            "This component requires the configuration of a Service Account to make authorized API calls " +
            "on behalf of the user. More info about Service Accounts and how they can be created and configured can " +
            "be found at the following <a href=\"https://cloud.google.com/iam/docs/service-accounts\">link</a>.")
    private DriveConfiguration configuration;

    @Property("Folder name")
    @Hint("Invoices")
    @Example("Invoices")
    @Description("The name of the new folder to create.")
    private DynamicString folderName;

    @Property("Folder description")
    @Hint("My Invoices folder")
    @Example("My Invoices folder")
    @Description("A short description of the folder.")
    private DynamicString folderDescription;

    @Property("Parent Folder ID")
    @Hint("0BwwA4oUTeiV1TGRPeTVjaWRDY1E")
    @Example("0BwwA4oUTeiV1TGRPeTVjaWRDY1E")
    @Description("The ID of the parent folder which contains the new folder to create.\n" +
            "If not specified, the file will be placed directly in the Service Account's 'My Drive' folder. ")
    private DynamicString parentFolderId;

    @Reference
    ScriptEngineService scriptEngine;
    @Reference
    ConverterService converterService;

    DriveApi driveApi;

    @Override
    public void initialize() {
        driveApi = createApi();
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {

        String finalFolderName;
        if (isNullOrBlank(folderName)) {
            // We take it from the message payload. The payload might not be a string,
            // for example when we upload the File ID from a rest listener and we forget
            // the mime type, therefore we have to convert it to a string type.
            Object payload = message.payload(); // The payload might not be a string.

            Input.requireTypeMatchesAny(FileDownload.class, payload, String.class, byte[].class);

            finalFolderName = converterService.convert(payload, String.class);

        } else {
            finalFolderName = scriptEngine.evaluate(folderName, flowContext, message)
                    .orElseThrow(() -> new FolderCreateException(FOLDER_NAME_NULL.format(folderName.value())));
        }

        String finalFolderDescription = scriptEngine.evaluate(folderDescription, flowContext, message)
                .orElse(null); // Not mandatory.

        String finalParentFolderId = scriptEngine.evaluate(parentFolderId, flowContext, message)
                .orElse(null); // Not mandatory.

        FolderCreateCommand command =
                new FolderCreateCommand(finalFolderName, finalFolderDescription, finalParentFolderId);

        File folder = driveApi.execute(command);

        FolderCreateAttributes attributes = new FolderCreateAttributes(folder);

        return MessageBuilder.get(FolderCreate.class)
                .withString(folder.getId(), MimeType.TEXT_PLAIN)
                .attributes(attributes)
                .build();
    }

    public void setFolderDescription(DynamicString folderDescription) {
        this.folderDescription = folderDescription;
    }

    public void setConfiguration(DriveConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setParentFolderId(DynamicString parentFolderId) {
        this.parentFolderId = parentFolderId;
    }

    public void setFolderName(DynamicString folderName) {
        this.folderName = folderName;
    }

    DriveApi createApi() {
        return DriveApiFactory.create(FolderCreate.class, configuration);
    }
}
