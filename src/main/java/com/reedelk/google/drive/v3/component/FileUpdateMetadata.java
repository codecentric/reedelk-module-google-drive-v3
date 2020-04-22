package com.reedelk.google.drive.v3.component;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.reedelk.google.drive.v3.internal.DriveService;
import com.reedelk.google.drive.v3.internal.exception.FileUpdateException;
import com.reedelk.runtime.api.annotation.Description;
import com.reedelk.runtime.api.annotation.ModuleComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.flow.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.script.ScriptEngineService;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.IOException;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ModuleComponent("Google File Update Metadata")
@Component(service = FileUpdateMetadata.class, scope = PROTOTYPE)
public class FileUpdateMetadata implements ProcessorSync {

    @Property("Configuration")
    @Description("The Google Drive Configuration providing the Service Account Credentials file.")
    private DriveConfiguration configuration;

    @Property("File ID")
    @Description("The ID of the file we want to update metadata. " +
            "If empty, the file ID is taken from the message payload.")
    private DynamicString fileId;

    @Property("File Name")
    private DynamicString fileName;

    @Property("File Description")
    private DynamicString fileDescription;

    @Reference
    private ScriptEngineService scriptEngine;


    private Drive drive;

    @Override
    public void initialize() {
        drive = DriveService.create(FileList.class, configuration);
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {

        String realFileId = scriptEngine.evaluate(fileId, flowContext, message)
                .orElseThrow(() -> new FileUpdateException("File ID must not be null."));

        File fileMetadata = new File();
        scriptEngine.evaluate(fileName, flowContext, message).ifPresent(fileMetadata::setName);
        scriptEngine.evaluate(fileDescription, flowContext, message).ifPresent(fileMetadata::setDescription);

        File updatedFile;
        try {
            updatedFile = drive.files().update(realFileId, fileMetadata)
                    .setFields("*")
                    .execute();

        } catch (IOException exception) {
            String error = ""; // TODO: Here
            throw new FileUpdateException(error, exception);
        }

        return MessageBuilder.get(FileUpdateContent.class)
                .withString(updatedFile.getId(), MimeType.TEXT_PLAIN)
                .build();
    }

    public void setConfiguration(DriveConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setFileId(DynamicString fileId) {
        this.fileId = fileId;
    }

    public void setFileName(DynamicString fileName) {
        this.fileName = fileName;
    }

    public void setFileDescription(DynamicString fileDescription) {
        this.fileDescription = fileDescription;
    }
}
