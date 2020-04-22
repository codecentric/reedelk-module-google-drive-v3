package com.reedelk.google.drive.v3.component;

import com.google.api.services.drive.Drive;
import com.reedelk.google.drive.v3.internal.DriveService;
import com.reedelk.google.drive.v3.internal.exception.FileDeleteException;
import com.reedelk.runtime.api.annotation.Description;
import com.reedelk.runtime.api.annotation.ModuleComponent;
import com.reedelk.runtime.api.annotation.Property;
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

import static com.reedelk.runtime.api.commons.DynamicValueUtils.isNullOrBlank;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ModuleComponent("Google File Delete")
@Component(service = FileDelete.class, scope = PROTOTYPE)
public class FileDelete implements ProcessorSync {

    @Property("Configuration")
    private DriveConfiguration configuration;

    @Property("File ID")
    @Description("The ID of the file or shared drive we want to create this permission for. " +
            "If empty, the file ID is taken from the message payload.")
    private DynamicString fileId;

    @Reference
    private ScriptEngineService scriptEngine;
    @Reference
    private ConverterService converterService;

    private Drive drive;

    @Override
    public void initialize() {
        drive = DriveService.create(FileDelete.class, configuration);
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

        Drive.Files.Delete delete;
        try {
            delete = drive.files().delete(realFileId);
        } catch (IOException e) {
            throw new FileDeleteException(e.getMessage(), e);
        }

        String deletedFileId = delete.getFileId();
        return MessageBuilder.get(FileDelete.class)
                .withString(deletedFileId, MimeType.TEXT_PLAIN)
                .build();
    }
}
