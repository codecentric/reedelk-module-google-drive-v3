package com.reedelk.google.drive.v3.component;

import com.reedelk.google.drive.v3.internal.command.FileDownloadCommand;
import com.reedelk.google.drive.v3.internal.command.PermissionDeleteCommand;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageAttributes;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.message.content.TypedContent;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PermissionDeleteTest extends AbstractComponentTest {

    @Captor
    protected ArgumentCaptor<PermissionDeleteCommand> captor = ArgumentCaptor.forClass(PermissionDeleteCommand.class);

    private PermissionDelete component = spy(new PermissionDelete());

    @BeforeEach
    void setUp() {
        super.setUp();
        component.driveApi = driveApi;
        component.scriptEngine = scriptEngine;
        component.converterService = converterService;
    }

    @Test
    void shouldInvokeCommandWithCorrectArgumentsWhenFileIdPropertyIsPresent() {
        // Given
        String fileId = UUID.randomUUID().toString();
        String permissionId = UUID.randomUUID().toString();
        component.setFileId(DynamicString.from(fileId));
        component.setPermissionId(DynamicString.from(permissionId));

        // When
        component.apply(context, message);

        // Then
        verify(driveApi).execute(captor.capture());
        PermissionDeleteCommand value = captor.getValue();

        assertThat(value).hasFieldOrPropertyWithValue("fileId", fileId);
        assertThat(value).hasFieldOrPropertyWithValue("permissionId", permissionId);
    }

    @Test
    void shouldReturnCorrectPayloadAndAttributes() {
        // Given
        String fileId = UUID.randomUUID().toString();
        String permissionId = UUID.randomUUID().toString();
        component.setFileId(DynamicString.from(fileId));
        component.setPermissionId(DynamicString.from(permissionId));

        // When
        Message actual = component.apply(context, message);

        // Then
        String payload = actual.payload();
        assertThat(payload).isEqualTo(permissionId);

        MessageAttributes attributes = actual.attributes();
        assertThat(attributes).containsEntry("id", permissionId);
        assertThat(attributes).containsEntry("fileId", fileId);
    }

    @Test
    void shouldInvokeCommandWithCorrectArgumentsWhenPermissionIdFromInputMessagePayload() {
        // Given
        String permissionId = UUID.randomUUID().toString();
        doReturn(permissionId).when(message).payload();

        String fileId = UUID.randomUUID().toString();
        component.setFileId(DynamicString.from(fileId));

        // When
        component.apply(context, message);

        // Then
        verify(driveApi).execute(captor.capture());
        PermissionDeleteCommand value = captor.getValue();

        assertThat(value).hasFieldOrPropertyWithValue("fileId", fileId);
        assertThat(value).hasFieldOrPropertyWithValue("permissionId", permissionId);
    }
}