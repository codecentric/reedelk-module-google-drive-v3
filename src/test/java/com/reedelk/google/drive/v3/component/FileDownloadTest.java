package com.reedelk.google.drive.v3.component;

import com.reedelk.google.drive.v3.internal.command.FileDownloadCommand;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageAttributes;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class FileDownloadTest extends AbstractComponentTest {

    @Captor
    protected ArgumentCaptor<FileDownloadCommand> captor = ArgumentCaptor.forClass(FileDownloadCommand.class);

    private FileDownload component = new FileDownload();

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
        String fileIdToDownload = UUID.randomUUID().toString();
        component.setFileId(DynamicString.from(fileIdToDownload));

        // When
        component.apply(context, message);

        // Then
        verify(driveApi).execute(captor.capture());
        FileDownloadCommand value = captor.getValue();

        assertThat(value).hasFieldOrPropertyWithValue("fileId", fileIdToDownload);
    }

    @Test
    void shouldInvokeCommandWithCorrectArgumentsWhenFileIdFromInputMessagePayload() {
        // Given
        String fileIdToDownload = UUID.randomUUID().toString();
        Message input = MessageBuilder.get(TestComponent.class)
                .withJavaObject(fileIdToDownload)
                .build();

        // When
        component.apply(context, input);

        // Then
        verify(driveApi).execute(captor.capture());
        FileDownloadCommand value = captor.getValue();

        assertThat(value).hasFieldOrPropertyWithValue("fileId", fileIdToDownload);
    }

    @Test
    void shouldReturnCorrectPayloadAndAttributes() {
        // Given
        String fileContent = "My content";

        String fileIdToDownload = UUID.randomUUID().toString();
        component.setFileId(DynamicString.from(fileIdToDownload));

        doReturn(fileContent.getBytes())
                .when(driveApi)
                .execute(any(FileDownloadCommand.class));

        // When
        Message actual = component.apply(context, message);

        // Then
        MessageAttributes attributes = actual.getAttributes();
        byte[] payload = actual.payload();

        assertThat(payload).isEqualTo(fileContent.getBytes());
        assertThat(attributes).containsEntry("id", fileIdToDownload);
    }
}
