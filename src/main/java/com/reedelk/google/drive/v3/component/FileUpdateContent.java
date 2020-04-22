package com.reedelk.google.drive.v3.component;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.reedelk.google.drive.v3.internal.DriveService;
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

import java.io.IOException;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ModuleComponent("Google File Update Content")
@Component(service = FileUpdateContent.class, scope = PROTOTYPE)
public class FileUpdateContent implements ProcessorSync {

    @Property("Configuration")
    @Description("The Google Drive Configuration providing the Service Account Credentials file.")
    private DriveConfiguration configuration;

    @Property("File ID")
    @Description("The ID of the file we want to delete. " +
            "If empty, the file ID is taken from the message payload.")
    private DynamicString fileId;

    @Property("Mime Type")
    @DefaultValue(MimeType.AsString.TEXT_PLAIN)
    @MimeTypeCombo
    private String mimeType;

    @Reference
    private ScriptEngineService scriptEngine;
    @Reference
    private ConverterService converterService;

    private MimeType fileMimeType;
    private Drive drive;

    @Override
    public void initialize() {
        drive = DriveService.create(FileList.class, configuration);
        fileMimeType = MimeType.parse(mimeType, MimeType.TEXT_PLAIN);
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {

        String realFileId = scriptEngine.evaluate(fileId, flowContext, message)
                .orElseThrow(() -> new FileUpdateException("File ID must not be null."));

        Object payload = message.payload();

        byte[] fileContent = converterService.convert(payload, byte[].class);

        ByteArrayContent byteArrayContent =
                new ByteArrayContent(fileMimeType.toString(), fileContent);

        File updatedFile;
        try {
            updatedFile = drive.files().update(realFileId, null, byteArrayContent)
                    .setFields("*")
                    .execute();
        } catch (IOException exception) {
            String error = "";
            throw new FileUpdateException(error, exception);
        }

        return MessageBuilder.get(FileUpdateContent.class)
                .withString(updatedFile.getId(), MimeType.TEXT_PLAIN)
                .build();
    }
}
