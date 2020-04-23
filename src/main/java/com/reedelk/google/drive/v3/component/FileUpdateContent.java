package com.reedelk.google.drive.v3.component;

import com.reedelk.google.drive.v3.internal.DriveApi;
import com.reedelk.google.drive.v3.internal.DriveApiFactory;
import com.reedelk.google.drive.v3.internal.FileUpdateContentAttributes;
import com.reedelk.google.drive.v3.internal.exception.FileUpdateContentException;
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

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ModuleComponent("Drive File Update Content")
@Component(service = FileUpdateContent.class, scope = PROTOTYPE)
public class FileUpdateContent implements ProcessorSync {

    @Property("Configuration")
    @Description("The Google Service Account Configuration to be used to connect to Google Drive." +
            "This component requires the configuration of a Service Account to make authorized API calls " +
            "on behalf of the user. More info about Service Accounts and how they can be configured can " +
            "be found at the following <a href=\"https://cloud.google.com/iam/docs/service-accounts\">link</a>.")
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

    private DriveApi driveApi;

    @Override
    public void initialize() {
        driveApi = DriveApiFactory.create(FileUpdateContent.class, configuration);
        fileMimeType = MimeType.parse(mimeType, MimeType.TEXT_PLAIN);
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {

        String realFileId = scriptEngine.evaluate(fileId, flowContext, message)
                .orElseThrow(() -> new FileUpdateContentException("File ID must not be null."));

        Object payload = message.payload();

        byte[] fileContent = converterService.convert(payload, byte[].class);

        driveApi.fileUpdate(realFileId, fileMimeType, fileContent);

        FileUpdateContentAttributes attributes = new FileUpdateContentAttributes(realFileId);

        return MessageBuilder.get(FileUpdateContent.class)
                .withString(realFileId, MimeType.TEXT_PLAIN)
                .attributes(attributes)
                .build();
    }
}
