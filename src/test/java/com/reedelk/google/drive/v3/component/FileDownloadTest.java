package com.reedelk.google.drive.v3.component;

import com.reedelk.google.drive.v3.internal.command.FileDownloadCommand;
import com.reedelk.google.drive.v3.internal.exception.FileDownloadException;
import com.reedelk.runtime.api.commons.ModuleContext;
import com.reedelk.runtime.api.exception.ComponentInputException;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class FileDownloadTest extends AbstractComponentTest {

    @Captor
    protected ArgumentCaptor<FileDownloadCommand> captor = ArgumentCaptor.forClass(FileDownloadCommand.class);

    private FileDownload component = spy(new FileDownload());

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
        String fileIdToDownload = UUID.randomUUID().toString();
        component.setFileId(DynamicString.from(fileIdToDownload));
        component.initialize();

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

        component.initialize();

        // When
        component.apply(context, input);

        // Then
        verify(driveApi).execute(captor.capture());
        FileDownloadCommand value = captor.getValue();

        assertThat(value).hasFieldOrPropertyWithValue("fileId", fileIdToDownload);
    }

    @Test
    void shouldReturnCorrectPayloadWithDefaultMimeTypeAndAttributes() {
        // Given
        String fileContent = "My content";

        String fileIdToDownload = UUID.randomUUID().toString();
        component.setFileId(DynamicString.from(fileIdToDownload));
        component.initialize();

        doReturn(fileContent.getBytes())
                .when(driveApi)
                .execute(any(FileDownloadCommand.class));

        // When
        Message actual = component.apply(context, message);

        // Then
        byte[] payload = actual.payload();
        assertThat(payload).isEqualTo(fileContent.getBytes());

        MessageAttributes attributes = actual.getAttributes();
        assertThat(attributes).containsEntry("id", fileIdToDownload);

        TypedContent<Object, Object> content = actual.getContent();
        assertThat(content.getMimeType()).isEqualTo(MimeType.APPLICATION_BINARY);
    }

    @Test
    void shouldReturnCorrectPayloadWithGivenMimeTypeAndAttributes() {
        // Given
        String fileContent = "My content";

        String fileIdToDownload = UUID.randomUUID().toString();
        component.setFileId(DynamicString.from(fileIdToDownload));
        component.setMimeType(MimeType.AsString.TEXT_XML);
        component.initialize();

        doReturn(fileContent.getBytes())
                .when(driveApi)
                .execute(any(FileDownloadCommand.class));

        // When
        Message actual = component.apply(context, message);

        // Then
        String payload = actual.payload();
        assertThat(payload).isEqualTo(fileContent);

        MessageAttributes attributes = actual.getAttributes();
        assertThat(attributes).containsEntry("id", fileIdToDownload);

        TypedContent<Object, Object> content = actual.getContent();
        assertThat(content.getMimeType()).isEqualTo(MimeType.TEXT_XML);
    }

    @Test
    void shouldThrowExceptionWhenFileIdEvaluatesEmpty() {
        // Given
        DynamicString fileIdExpression = DynamicString.from("#[message.payload()]", new ModuleContext(10L));
        component.setFileId(fileIdExpression);
        component.initialize();

        doAnswer(invocation -> Optional.empty())
                .when(scriptEngine)
                .evaluate(fileIdExpression, context, message);

        // When
        FileDownloadException thrown =
                assertThrows(FileDownloadException.class, () -> component.apply(context, message));

        // Then
        assertThat(thrown)
                .hasMessage("The File ID was null: I cannot download a file with null ID (DynamicValue=[#[message.payload()]]).");
    }

    @Test
    void shouldThrowExceptionWhenPayloadInputIsLong() {
        // Given
        component.initialize();

        Message input = MessageBuilder.get(TestComponent.class)
                .withJavaObject(10L)
                .build();

        // When
        ComponentInputException thrown =
                assertThrows(ComponentInputException.class, () -> component.apply(context, input));

        // Then
        assertThat(thrown)
                .hasMessage("FileDownload (com.reedelk.google.drive.v3.component.FileDownload) " +
                        "was invoked with a not supported Input Type: actual=[Long], expected one of=[String,byte[]].");
    }
}
