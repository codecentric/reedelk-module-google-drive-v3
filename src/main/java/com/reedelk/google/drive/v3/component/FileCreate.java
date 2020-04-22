package com.reedelk.google.drive.v3.component;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.reedelk.google.drive.v3.internal.DriveService;
import com.reedelk.google.drive.v3.internal.commons.Utils;
import com.reedelk.google.drive.v3.internal.exception.FileCreateException;
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

import java.io.IOException;
import java.util.Optional;

import static com.reedelk.google.drive.v3.internal.commons.Messages.FileCreate.GENERIC_ERROR;
import static com.reedelk.runtime.api.commons.ConfigurationPreconditions.requireNotNull;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ModuleComponent("Google File Create")
@Component(service = FileCreate.class, scope = PROTOTYPE)
public class FileCreate implements ProcessorSync {

    @Property("Configuration")
    @Description("The Google Service Account Configuration to be used to connect to Google Drive." +
            "This component requires the configuration of a Service Account to make authorized API calls " +
            "on behalf of the user. More info about Service Accounts and how they can be configured can " +
            "be found at the following <a href=\"https://cloud.google.com/iam/docs/service-accounts\">link</a>.")
    private DriveConfiguration configuration;

    @Property("File name")
    private DynamicString fileName;

    @Property("File description")
    private DynamicString fileDescription;

    @Property("Mime Type")
    @DefaultValue(MimeType.AsString.TEXT_PLAIN)
    @MimeTypeCombo
    private String mimeType;

    private Drive drive;

    @Reference
    private ScriptEngineService scriptEngine;
    @Reference
    private ConverterService converterService;

    private MimeType fileMimeType;

    @Override
    public void initialize() {
        requireNotNull(FileCreate.class, fileName, "Google Drive File name must not be empty.");
        drive = DriveService.create(FileCreate.class, configuration);
        fileMimeType = MimeType.parse(mimeType, MimeType.TEXT_PLAIN);
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {

        File fileMetadata = new File();

        scriptEngine.evaluate(fileName, flowContext, message).ifPresent(fileMetadata::setName);
        scriptEngine.evaluate(fileDescription, flowContext, message).ifPresent(fileMetadata::setDescription);

        Object payload = message.payload();

        byte[] fileContent = converterService.convert(payload, byte[].class);

        ByteArrayContent byteArrayContent =
                new ByteArrayContent(fileMimeType.toString(), fileContent);

        File file;
        try {
            file = drive.files().create(fileMetadata, byteArrayContent)
                    .setFields(Utils.ALL_FIELDS)
                    .execute();
        } catch (IOException exception) {
            String actualFileName = Optional.ofNullable(fileMetadata.getName()).orElse("file name not set");
            String error = GENERIC_ERROR.format(actualFileName, exception.getMessage());
            throw new FileCreateException(error, exception);
        }

        String newFileId = file.getId();
        return MessageBuilder.get(FileCreate.class)
                .withString(newFileId, MimeType.TEXT_PLAIN)
                .build();
    }

    public void setFileDescription(DynamicString fileDescription) {
        this.fileDescription = fileDescription;
    }

    public void setConfiguration(DriveConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setFileName(DynamicString fileName) {
        this.fileName = fileName;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
