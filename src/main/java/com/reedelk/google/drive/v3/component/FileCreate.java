package com.reedelk.google.drive.v3.component;

import com.google.api.services.drive.model.File;
import com.reedelk.google.drive.v3.internal.DriveApi;
import com.reedelk.google.drive.v3.internal.DriveApiFactory;
import com.reedelk.google.drive.v3.internal.FileCreateAttributes;
import com.reedelk.google.drive.v3.internal.exception.FileCreateException;
import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.commons.MimeTypeUtils;
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
import static com.reedelk.runtime.api.commons.ConfigurationPreconditions.requireNotNull;
import static java.util.Optional.ofNullable;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ModuleComponent("Drive File Create")
@Component(service = FileCreate.class, scope = PROTOTYPE)
@Description("Creates a new file in Google Drive with the given name and optional description. " +
        "The content of the file is taken from the input message payload and if the file was successfully created " +
        "the ID of the file is returned in the output message payload. The default file owner will be the provided Service Account and permissions " +
        "on the file must be used in order to assign further read/write permissions to other users. New permissions can be " +
        "created using the 'Drive Permission Create' component. " +
        "This component requires the configuration of a Service Account to make authorized API calls " +
        "on behalf of the user. The component's configuration uses the private key (in JSON format) " +
        "of the Google Service Account which can be generated and downloaded from the Service Account page. " +
        "More info about Service Accounts and how they can be created and configured can " +
        "be found in the official Google Service Accounts <a href=\"https://cloud.google.com/iam/docs/service-accounts\">Documentation</a> page.")
public class FileCreate implements ProcessorSync {

    @Property("Configuration")
    @Description("The Google Service Account Configuration to be used to connect to Google Drive." +
            "This component requires the configuration of a Service Account to make authorized API calls " +
            "on behalf of the user. More info about Service Accounts and how they can be created and configured can " +
            "be found at the following <a href=\"https://cloud.google.com/iam/docs/service-accounts\">link</a>.")
    private DriveConfiguration configuration;

    @Property("File name")
    @Description("")
    private DynamicString fileName;

    @Property("File description")
    private DynamicString fileDescription;

    @Property("Auto mime type")
    @Example("true")
    @InitValue("true")
    @DefaultValue("false")
    @Description("If true, the mime type of the payload is determined from the extension of the resource read.")
    private boolean autoMimeType;

    @Property("Mime type")
    @MimeTypeCombo
    @Example(MimeType.AsString.IMAGE_JPEG)
    @DefaultValue(MimeType.AsString.ANY)
    @When(propertyName = "autoMimeType", propertyValue = "false")
    @When(propertyName = "autoMimeType", propertyValue = When.BLANK)
    @Description("The mime type of the file to be created on Google Drive.")
    private String mimeType;

    @Property("Use content as indexable text")
    @DefaultValue("false")
    @Group("Indexing")
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
        requireNotNull(FileCreate.class, fileName, "Google Drive File name must not be empty.");
        driveApi = DriveApiFactory.create(FileCreate.class, configuration);
        realIndexableText = ofNullable(indexableText).orElse(CONTENT_AS_INDEXABLE_TEXT);
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {

        String finalFileName = scriptEngine.evaluate(fileName, flowContext, message)
                .orElseThrow(() -> new FileCreateException("File name must not be empty"));

        String finalFileDescription = scriptEngine.evaluate(fileDescription, flowContext, message).orElse(null);

        MimeType finalMimeType =
                MimeTypeUtils.fromFileExtensionOrParse(autoMimeType, mimeType, finalFileName, MimeType.APPLICATION_BINARY);

        Object payload = message.payload();

        byte[] fileContent = converterService.convert(payload, byte[].class);

        File file = driveApi.fileCreate(
                finalFileName,
                finalFileDescription,
                finalMimeType,
                realIndexableText,
                fileContent);

        FileCreateAttributes attributes = new FileCreateAttributes(file);
        return MessageBuilder.get(FileCreate.class)
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

    public void setAutoMimeType(boolean autoMimeType) {
        this.autoMimeType = autoMimeType;
    }

    public void setFileName(DynamicString fileName) {
        this.fileName = fileName;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setIndexableText(Boolean indexableText) {
        this.indexableText = indexableText;
    }
}
