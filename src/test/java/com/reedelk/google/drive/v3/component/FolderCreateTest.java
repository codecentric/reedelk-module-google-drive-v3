package com.reedelk.google.drive.v3.component;

import com.google.api.services.drive.model.File;
import com.reedelk.google.drive.v3.internal.command.FolderCreateCommand;
import com.reedelk.google.drive.v3.internal.exception.FolderCreateException;
import com.reedelk.runtime.api.commons.ModuleContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageAttributes;
import com.reedelk.runtime.api.message.MessageBuilder;
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

class FolderCreateTest extends AbstractComponentTest {

    @Captor
    protected ArgumentCaptor<FolderCreateCommand> captor = ArgumentCaptor.forClass(FolderCreateCommand.class);

    private FolderCreate component = spy(new FolderCreate());

    @BeforeEach
    void setUp() {
        super.setUp();
        component.scriptEngine = scriptEngine;
        component.converterService = converterService;
        doReturn(driveApi).when(component).createApi();
    }

    @Test
    void shouldInvokeCommandWithCorrectArgumentsWhenFileIdPropertyIsPresent() {
        // Given
        String folderName = "folder-" + UUID.randomUUID().toString();
        String folderDescription = folderName + " Description";
        String parentFolderId = UUID.randomUUID().toString();

        component.setFolderName(DynamicString.from(folderName));
        component.setParentFolderId(DynamicString.from(parentFolderId));
        component.setFolderDescription(DynamicString.from(folderDescription));
        component.initialize();

        File file = new File();
        doReturn(file)
                .when(driveApi)
                .execute(any(FolderCreateCommand.class));
        // When
        component.apply(context, message);

        // Then
        verify(driveApi).execute(captor.capture());
        FolderCreateCommand value = captor.getValue();

        assertThat(value).hasFieldOrPropertyWithValue("folderName", folderName);
        assertThat(value).hasFieldOrPropertyWithValue("parentFolderId", parentFolderId);
        assertThat(value).hasFieldOrPropertyWithValue("folderDescription", folderDescription);
    }

    @Test
    void shouldInvokeCommandWithCorrectArgumentsWhenFolderNameFromInputMessagePayload() {
        // Given
        String folderName = "folder-" + UUID.randomUUID().toString();
        Message input = MessageBuilder.get(TestComponent.class)
                .withJavaObject(folderName)
                .build();
        component.initialize();

        File file = new File();
        doReturn(file)
                .when(driveApi)
                .execute(any(FolderCreateCommand.class));

        // When
        component.apply(context, input);

        // Then
        verify(driveApi).execute(captor.capture());
        FolderCreateCommand value = captor.getValue();

        assertThat(value).hasFieldOrPropertyWithValue("folderName", folderName);
    }

    @Test
    void shouldInvokeCommandWithCorrectDefaultArguments() {
        // Given
        String folderName = "folder-" + UUID.randomUUID().toString();

        component.setFolderName(DynamicString.from(folderName));
        component.initialize();

        File file = new File();
        doReturn(file)
                .when(driveApi)
                .execute(any(FolderCreateCommand.class));
        // When
        component.apply(context, message);

        // Then
        verify(driveApi).execute(captor.capture());
        FolderCreateCommand value = captor.getValue();

        assertThat(value).hasFieldOrPropertyWithValue("folderName", folderName);
        assertThat(value).hasFieldOrPropertyWithValue("parentFolderId", null);
        assertThat(value).hasFieldOrPropertyWithValue("folderDescription", null);
    }

    @Test
    void shouldReturnCorrectPayloadAndAttributes() {
        // Given
        String folderName = "folder-" + UUID.randomUUID().toString();

        component.setFolderName(DynamicString.from(folderName));
        component.initialize();

        String folderId = UUID.randomUUID().toString();
        String webContentLink = "myWebContentLink";
        String webViewLink = "myWebViewLink";
        File file = new File()
                .setId(folderId)
                .setName(folderName)
                .setWebContentLink(webContentLink)
                .setWebViewLink(webViewLink);

        doReturn(file)
                .when(driveApi)
                .execute(any(FolderCreateCommand.class));
        // When
        Message actual = component.apply(context, message);

        // Then
        String id = actual.payload();
        assertThat(id).isEqualTo(folderId);

        MessageAttributes attributes = actual.attributes();
        assertThat(attributes).containsEntry("webContentLink", webContentLink);
        assertThat(attributes).containsEntry("webViewLink", webViewLink);
        assertThat(attributes).containsEntry("id", folderId);
        assertThat(attributes).containsEntry("name", folderName);
    }

    @Test
    void shouldThrowExceptionWhenFolderNameEvaluatesEmpty() {
        // Given
        DynamicString folderNameExpression = DynamicString.from("#[message.payload()]", new ModuleContext(10L));
        component.setFolderName(folderNameExpression);
        component.initialize();

        doAnswer(invocation -> Optional.empty())
                .when(scriptEngine)
                .evaluate(folderNameExpression, context, message);

        // When
        FolderCreateException thrown =
                assertThrows(FolderCreateException.class, () -> component.apply(context, message));

        // Then
        assertThat(thrown)
                .hasMessage("The Folder name was empty: I cannot create a folder with " +
                        "an empty name (DynamicValue=[#[message.payload()]]).");
    }
}