package com.reedelk.google.drive.v3.component;

import com.reedelk.google.drive.v3.internal.DriveApi;
import com.reedelk.google.drive.v3.internal.DriveApiFactory;
import com.reedelk.google.drive.v3.internal.attribute.FileReadAttributes;
import com.reedelk.google.drive.v3.internal.command.FileReadCommand;
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

import static com.reedelk.runtime.api.commons.DynamicValueUtils.isNullOrBlank;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ModuleComponent("Drive File Read")
@Component(service = FileRead.class, scope = PROTOTYPE)
@Description("Reads the content of the file with the given file ID from Google Drive. " +
        "Optional search filters and order by sort keys can be applied to filter and order the returned results. " +
        "If the number of returned files is potentially large it is recommended to use pagination by setting the page size and page token for subsequent listings." +
        "This component requires the configuration of a Service Account to make authorized API calls " +
        "on behalf of the user. The component's configuration uses the private key (in JSON format) " +
        "of the Google Service Account which can be generated and downloaded from the Service Account page. " +
        "More info about Service Accounts and how they can be created and configured can " +
        "be found in the official Google Service Accounts <a href=\"https://cloud.google.com/iam/docs/service-accounts\">Documentation</a> page.")
public class FileRead implements ProcessorSync {

    @Property("Configuration")
    @Description("The Google Service Account Configuration to be used to connect to Google Drive." +
            "This component requires the configuration of a Service Account to make authorized API calls " +
            "on behalf of the user. More info about Service Accounts and how they can be configured can " +
            "be found at the following <a href=\"https://cloud.google.com/iam/docs/service-accounts\">link</a>.")
    private DriveConfiguration configuration;

    @Property("File ID")
    @Description("The ID of the file to read the content from. " +
            "If not defined, the file ID is taken from the message payload.")
    private DynamicString fileId;

    @Property("Mime type")
    @MimeTypeCombo
    @Example(MimeType.AsString.IMAGE_JPEG)
    @DefaultValue(MimeType.AsString.APPLICATION_BINARY)
    @Description("The mime type of the file retrieved from Google Drive.")
    private String mimeType;

    @Reference
    private ScriptEngineService scriptEngine;
    @Reference
    private ConverterService converterService;

    private DriveApi driveApi;
    private MimeType finalMimeType;

    @Override
    public void initialize() {
        driveApi = DriveApiFactory.create(FileRead.class, configuration);
        finalMimeType = MimeType.parse(mimeType, MimeType.APPLICATION_BINARY);
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
                    .orElseThrow(() -> new FileReadException("File ID must not be null."));
        }

        FileReadCommand command = new FileReadCommand(realFileId);

        byte[] content = driveApi.execute(command);

        FileReadAttributes attributes = new FileReadAttributes(realFileId);

        return MessageBuilder.get(FileRead.class)
                .attributes(attributes)
                .withBinary(content, finalMimeType)
                .build();
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
