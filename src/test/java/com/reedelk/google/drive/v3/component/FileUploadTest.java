package com.reedelk.google.drive.v3.component;

import com.google.api.services.drive.model.File;
import com.reedelk.google.drive.v3.internal.command.FileUploadCommand;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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

        File uploadedFile = new File()
                .setDescription(fileDescription);
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
}