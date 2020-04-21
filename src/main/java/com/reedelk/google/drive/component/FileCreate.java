package com.reedelk.google.drive.component;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.reedelk.google.drive.internal.DriveServiceProvider;
import com.reedelk.runtime.api.annotation.DefaultValue;
import com.reedelk.runtime.api.annotation.MimeTypeCombo;
import com.reedelk.runtime.api.annotation.ModuleComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.converter.ConverterService;
import com.reedelk.runtime.api.exception.PlatformException;
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

@ModuleComponent("Google File Create")
@Component(service = FileCreate.class, scope = PROTOTYPE)
public class FileCreate implements ProcessorSync {

    @Property("Configuration")
    private DriveConfiguration configuration;

    @Property("File name")
    private DynamicString name;

    @Property("Mime Type")
    @DefaultValue("text/plain")
    @MimeTypeCombo
    private String mimeType;

    private Drive drive;

    @Reference
    private ScriptEngineService scriptEngine;
    @Reference
    private ConverterService converterService;

    @Override
    public void initialize() {
        DriveServiceProvider provider = new DriveServiceProvider();
        this.drive = provider.get(FileCreate.class, configuration);

        // Add in the config precondition this stuf... DynamicValue and such.
        //DynamicValueUtils.isNullOrBlank()
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {

        String fileName = scriptEngine.evaluate(name, flowContext, message)
                .orElseThrow(() -> new PlatformException("File name must not be null."));

        Object payload = message.payload();

        byte[] fileContent = converterService.convert(payload, byte[].class);

        File fileMetadata = new File();
        fileMetadata.setName(fileName);
        ByteArrayContent byteArrayContent = new ByteArrayContent("text/plain", fileContent);
        try {
            File file = drive.files().create(fileMetadata, byteArrayContent)
                    .setFields("id")
                    .execute();

            String newFileId = file.getId();
            return MessageBuilder.get(FileCreate.class)
                    .withString(newFileId, MimeType.TEXT_PLAIN)
                    .build();

        } catch (IOException e) {
            throw new PlatformException(e);
        }
    }

    public void setName(DynamicString name) {
        this.name = name;
    }

    public void setConfiguration(DriveConfiguration configuration) {
        this.configuration = configuration;
    }

}
