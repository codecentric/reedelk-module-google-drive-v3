package com.reedelk.google.drive.v3.component;

import com.google.api.services.drive.model.File;
import com.reedelk.google.drive.v3.internal.command.FileUploadCommand;
import com.reedelk.google.drive.v3.internal.exception.FileDownloadException;
import com.reedelk.google.drive.v3.internal.exception.FileUploadException;
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

class FileUploadTest extends AbstractComponentTest {

    @Captor
    protected ArgumentCaptor<FileUploadCommand> captor = ArgumentCaptor.forClass(FileUploadCommand.class);

    private FileUpload component = spy(new FileUpload());

    @BeforeEach
    void setUp() {
        super.setUp();
        component.scriptEngine = scriptEngine;
        component.converterService = converterService;
        doReturn(driveApi).when(component).createApi();
    }

    @Test
    void shouldInvokeCommandWithCorrectArguments() {
        // Given
        String content = "My test file content";
        doReturn(content).when(message).payload();
        doReturn(content.getBytes()).when(converterService).convert(content, byte[].class);

        String fileName = "my-file.txt";
        String fileDescription = "My file description";
        String parentFolderId = UUID.randomUUID().toString();

        component.setFileName(DynamicString.from(fileName));
        component.setParentFolderId(DynamicString.from(parentFolderId));
        component.setFileDescription(DynamicString.from(fileDescription));
        component.setIndexableText(true);
        component.initialize();

        File uploadedFile = new File();
        doReturn(uploadedFile).when(driveApi).execute(any(FileUploadCommand.class));

        // When
        component.apply(context, message);

        // Then
        verify(driveApi).execute(captor.capture());
        FileUploadCommand value = captor.getValue();

        assertThat(value).hasFieldOrPropertyWithValue("fileDescription", fileDescription);
        assertThat(value).hasFieldOrPropertyWithValue("fileContent", content.getBytes());
        assertThat(value).hasFieldOrPropertyWithValue("parentFolderId", parentFolderId);
        assertThat(value).hasFieldOrPropertyWithValue("indexableText", true);
        assertThat(value).hasFieldOrPropertyWithValue("fileName", fileName);
    }

    @Test
    void shouldInvokeCommandWithDefaultArguments() {
        // Given
        String content = "My test file content";
        doReturn(content).when(message).payload();
        doReturn(content.getBytes()).when(converterService).convert(content, byte[].class);

        String fileName = "my-file.txt";

        component.setFileName(DynamicString.from(fileName));
        component.initialize();

        File uploadedFile = new File();
        doReturn(uploadedFile).when(driveApi).execute(any(FileUploadCommand.class));

        // When
        component.apply(context, message);

        // Then
        verify(driveApi).execute(captor.capture());
        FileUploadCommand value = captor.getValue();

        assertThat(value).hasFieldOrPropertyWithValue("fileContent", content.getBytes());
        assertThat(value).hasFieldOrPropertyWithValue("fileDescription", null);
        assertThat(value).hasFieldOrPropertyWithValue("parentFolderId", null);
        assertThat(value).hasFieldOrPropertyWithValue("indexableText", false);
        assertThat(value).hasFieldOrPropertyWithValue("fileName", fileName);
    }

    @Test
    void shouldReturnCorrectPayloadAndAttributes() {
        // Given
        String content = "My test file content";
        doReturn(content).when(message).payload();
        doReturn(content.getBytes()).when(converterService).convert(content, byte[].class);

        String fileName = "my-file.txt";

        component.setFileName(DynamicString.from(fileName));
        component.initialize();

        String expectedFileId = UUID.randomUUID().toString();
        File uploadedFile = new File().setId(expectedFileId);
        doReturn(uploadedFile).when(driveApi).execute(any(FileUploadCommand.class));

        // When
        Message actual = component.apply(context, message);

        // Then
        String actualFileId = actual.payload();
        assertThat(actualFileId).isEqualTo(expectedFileId);

        MessageAttributes attributes = actual.attributes();
        assertThat(attributes).containsEntry("id", expectedFileId);
    }

    @Test
    void shouldUploadFileWithEmptyBytesWhenPayloadIsNull() {
        // Given
        String content = null;
        doReturn(content).when(message).payload();
        doReturn(null).when(converterService).convert(content, byte[].class);

        String fileName = "my-file.txt";

        component.setFileName(DynamicString.from(fileName));
        component.initialize();

        File uploadedFile = new File();
        doReturn(uploadedFile).when(driveApi).execute(any(FileUploadCommand.class));

        // When
        component.apply(context, message);

        // Then
        verify(driveApi).execute(captor.capture());
        FileUploadCommand value = captor.getValue();

        assertThat(value).hasFieldOrPropertyWithValue("fileContent", new byte[0]);
    }

    @Test
    void shouldThrowExceptionWhenFileNameEvaluatesEmpty() {
        // Given
        DynamicString fileNameExpression = DynamicString.from("#[message.payload()]", new ModuleContext(10L));
        component.setFileName(fileNameExpression);
        component.initialize();

        doAnswer(invocation -> Optional.empty())
                .when(scriptEngine)
                .evaluate(fileNameExpression, context, message);

        // When
        FileUploadException thrown =
                assertThrows(FileUploadException.class, () -> component.apply(context, message));

        // Then
        assertThat(thrown)
                .hasMessage("The File name was empty: I cannot upload a file with an empty file name (DynamicValue=[#[message.payload()]]).");
    }
}