package com.reedelk.google.drive.v3.component;

import com.google.api.services.drive.Drive;
import com.reedelk.google.drive.v3.internal.DriveService;
import com.reedelk.google.drive.v3.internal.exception.FileDeleteException;
import com.reedelk.google.drive.v3.internal.exception.FileReadException;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.reedelk.runtime.api.commons.DynamicValueUtils.isNullOrBlank;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ModuleComponent("Google File Read")
@Component(service = FileRead.class, scope = PROTOTYPE)
public class FileRead implements ProcessorSync {

    @Property("Configuration")
    @Description("The Google Drive Configuration providing the Service Account Credentials file.")
    private DriveConfiguration configuration;

    @Property("File ID")
    @Description("The ID of the file or shared drive we want to create this permission for. " +
            "If empty, the file ID is taken from the message payload.")
    private DynamicString fileId;

    @Property("Mime type")
    @MimeTypeCombo
    @Example(MimeType.AsString.IMAGE_JPEG)
    @DefaultValue(MimeType.AsString.APPLICATION_BINARY)
    @Description("The mime type of the file retrieved from Google Drive.")
    private String mimeType;

    private Drive drive;

    @Reference
    private ScriptEngineService scriptEngine;
    @Reference
    private ConverterService converterService;

    @Override
    public void initialize() {
        drive = DriveService.create(FileList.class, configuration);
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {

        String realFileId;
        if (isNullOrBlank(fileId)) {
            // We take it from the message payload.
            Object payload = message.payload(); // The payload might not be a string.
            realFileId = converterService.convert(payload, String.class);
        } else {
            realFileId = scriptEngine.evaluate(fileId, flowContext, message)
                    .orElseThrow(() -> new FileDeleteException("File ID must not be null."));
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){

            drive.files()
                    .get(realFileId)
                    .executeMediaAndDownloadTo(outputStream);

            byte[] fileContentAsBytes = outputStream.toByteArray();
            return MessageBuilder.get(FileRead.class)
                    .withBinary(fileContentAsBytes)
                    .build();

        } catch (IOException exception) {
            String error = ""; // TODO: Fixme.
            throw new FileReadException(error, exception);
        }
    }

    public void setConfiguration(DriveConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setFileId(DynamicString fileId) {
        this.fileId = fileId;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
