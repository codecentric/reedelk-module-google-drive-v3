package com.reedelk.google.drive.v3.component;

import com.reedelk.google.drive.v3.internal.command.FileUpdateCommand;
import com.reedelk.google.drive.v3.internal.exception.FileDownloadException;
import com.reedelk.google.drive.v3.internal.exception.FileUpdateException;
import com.reedelk.runtime.api.commons.ModuleContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageAttributes;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class FileUpdateTest extends AbstractComponentTest {

    @Captor
    protected ArgumentCaptor<FileUpdateCommand> captor = ArgumentCaptor.forClass(FileUpdateCommand.class);

    private FileUpdate component = new FileUpdate();

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
        String fileContent = "My updated text";
        doReturn(fileContent).when(message).payload();

        String fileIdToUpdate = UUID.randomUUID().toString();
        component.setFileId(DynamicString.from(fileIdToUpdate));

        doReturn(fileContent.getBytes())
                .when(converterService)
                .convert(fileContent, byte[].class);

        // When
        component.apply(context, message);

        // Then
        verify(driveApi).execute(captor.capture());
        FileUpdateCommand value = captor.getValue();

        assertThat(value).hasFieldOrPropertyWithValue("fileId", fileIdToUpdate);
        assertThat(value).hasFieldOrPropertyWithValue("fileContent", fileContent.getBytes());
    }

    // We expect that the file content is an empty byte array. If the payload
    // is null, we update the file with empty content.
    @Test
    void shouldInvokeCommandWithCorrectArgumentsWhenPayloadIsNull() {
        // Given
        String fileContent = null;
        doReturn(fileContent).when(message).payload();

        String fileIdToUpdate = UUID.randomUUID().toString();
        component.setFileId(DynamicString.from(fileIdToUpdate));

        doReturn(fileContent)
                .when(converterService)
                .convert(fileContent, byte[].class);

        // When
        component.apply(context, message);

        // Then
        verify(driveApi).execute(captor.capture());
        FileUpdateCommand value = captor.getValue();

        assertThat(value).hasFieldOrPropertyWithValue("fileId", fileIdToUpdate);
        assertThat(value).hasFieldOrPropertyWithValue("fileContent", new byte[0]);
    }

    @Test
    void shouldReturnCorrectPayloadAndAttributes() {
        // Given
        String fileContent = "My updated text";
        doReturn(fileContent).when(message).payload();

        String fileIdToUpdate = UUID.randomUUID().toString();
        component.setFileId(DynamicString.from(fileIdToUpdate));

        doReturn(fileContent.getBytes())
                .when(converterService)
                .convert(fileContent, byte[].class);

        // When
        Message actual = component.apply(context, message);

        // Then
        String fileId = actual.payload();
        assertThat(fileId).isEqualTo(fileIdToUpdate);

        MessageAttributes attributes = actual.attributes();
        assertThat(attributes).containsEntry("id", fileId);
    }

    @Test
    void shouldThrowExceptionWhenFileIdEvaluatesEmpty() {
        // Given
        DynamicString fileIdExpression = DynamicString.from("#[message.payload()]", new ModuleContext(10L));
        component.setFileId(fileIdExpression);

        doAnswer(invocation -> Optional.empty())
                .when(scriptEngine)
                .evaluate(fileIdExpression, context, message);

        // When
        FileUpdateException thrown =
                assertThrows(FileUpdateException.class, () -> component.apply(context, message));

        // Then
        assertThat(thrown)
                .hasMessage("The File ID was null: I cannot update a file with null ID (DynamicValue=[#[message.payload()]]).");
    }
}