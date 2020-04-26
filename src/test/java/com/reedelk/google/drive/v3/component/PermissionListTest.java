package com.reedelk.google.drive.v3.component;

import com.reedelk.google.drive.v3.internal.command.PermissionListCommand;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageAttributes;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PermissionListTest extends AbstractComponentTest {

    @Captor
    protected ArgumentCaptor<PermissionListCommand> captor = ArgumentCaptor.forClass(PermissionListCommand.class);

    private PermissionList component = spy(new PermissionList());

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
        component.setFileId(DynamicString.from(fileId));

        List<Map> permissions = new ArrayList<>();
        doReturn(permissions)
                .when(driveApi)
                .execute(any(PermissionListCommand.class));

        // When
        component.apply(context, message);

        // Then
        verify(driveApi).execute(captor.capture());
        PermissionListCommand value = captor.getValue();

        assertThat(value).hasFieldOrPropertyWithValue("fileId", fileId);
    }

    @Test
    void shouldReturnCorrectPayloadAndAttributes() {
        // Given
        String fileId = UUID.randomUUID().toString();
        component.setFileId(DynamicString.from(fileId));

        List<Map> permissions = new ArrayList<>();
        doReturn(permissions)
                .when(driveApi)
                .execute(any(PermissionListCommand.class));

        // When
        Message actual = component.apply(context, message);

        // Then
        List<Map> payload = actual.payload();
        assertThat(payload).isEqualTo(permissions);

        MessageAttributes attributes = actual.getAttributes();
        assertThat(attributes).containsEntry("fileId", fileId);
    }
}