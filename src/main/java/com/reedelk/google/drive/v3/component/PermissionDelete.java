package com.reedelk.google.drive.v3.component;

import com.google.api.services.drive.Drive;
import com.reedelk.google.drive.v3.internal.DriveService;
import com.reedelk.google.drive.v3.internal.exception.PermissionDeleteException;
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

@ModuleComponent("Google Permission Delete")
@Component(service = PermissionDelete.class, scope = PROTOTYPE)
public class PermissionDelete implements ProcessorSync {

    @Property("Configuration")
    private DriveConfiguration configuration;

    @Property("File ID")
    @Description("The ID of the file we want to change the permission. " +
            "If empty, the file ID is taken from the message payload.")
    private DynamicString fileId;

    @Property("Permission ID")
    @Description("The ID of the permission we want to delete. " +
            "If empty, the permission ID is taken from the message payload.")
    private DynamicString permissionId;

    private Drive drive;

    @Reference
    private ScriptEngineService scriptEngine;
    @Reference
    private ConverterService converterService;

    @Override
    public void initialize() {
        drive = DriveService.create(PermissionDelete.class, configuration);
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {

        String realFileId = scriptEngine.evaluate(fileId, flowContext, message)
                .orElseThrow(() -> new PermissionDeleteException("File ID must not be null."));

        String realPermissionId;
        if (isNullOrBlank(permissionId)) {
            // We take it from the message payload.
            Object payload = message.payload(); // The payload might not be a string.
            realPermissionId = converterService.convert(payload, String.class);
        } else {
            realPermissionId = scriptEngine.evaluate(permissionId, flowContext, message)
                    .orElseThrow(() -> new PermissionDeleteException("Permission ID must not be null."));
        }

        try {
            drive.permissions()
                    .delete(realFileId, realPermissionId)
                    .execute();
        } catch (IOException e) {
            throw new PermissionDeleteException(e.getMessage(), e);
        }

        // TODO: Attributes maybe should add the file and permission id in the attributes?
        // We return the removed permission ID.
        return MessageBuilder.get(FileDelete.class)
                .withString(realPermissionId, MimeType.TEXT_PLAIN)
                .build();
    }
}
