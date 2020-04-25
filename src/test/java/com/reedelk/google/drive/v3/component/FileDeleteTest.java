package com.reedelk.google.drive.v3.component;

import com.reedelk.google.drive.v3.internal.command.FileDeleteCommand;
import com.reedelk.google.drive.v3.internal.exception.FileDeleteException;
import com.reedelk.runtime.api.commons.ModuleContext;
import com.reedelk.runtime.api.exception.ComponentInputException;
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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;


class FileDeleteTest extends AbstractComponentTest {

    @Captor
    protected ArgumentCaptor<FileDeleteCommand> captor = ArgumentCaptor.forClass(FileDeleteCommand.class);

    private FileDelete component = new FileDelete();

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
        String fileIdToDelete = UUID.randomUUID().toString();
        component.setFileId(DynamicString.from(fileIdToDelete));

        // When
        component.apply(context, message);

        // Then
        verify(driveApi).execute(captor.capture());
        FileDeleteCommand value = captor.getValue();

        assertThat(value).hasFieldOrPropertyWithValue("fileId", fileIdToDelete);
    }

    @Test
    void shouldInvokeCommandWithCorrectArgumentsWhenFileIdFromInputMessagePayload() {
        // Given
        String fileIdToDelete = UUID.randomUUID().toString();
        Message input = MessageBuilder.get(TestComponent.class)
                .withJavaObject(fileIdToDelete)
                .build();

        // When
        component.apply(context, input);

        // Then
        verify(driveApi).execute(captor.capture());
        FileDeleteCommand value = captor.getValue();

        assertThat(value).hasFieldOrPropertyWithValue("fileId", fileIdToDelete);
    }

    @Test
    void shouldReturnCorrectPayloadAndAttributes() {
        // Given
        String fileIdToDelete = UUID.randomUUID().toString();
        component.setFileId(DynamicString.from(fileIdToDelete));

        // When
        Message actual = component.apply(context, message);

        // Then
        MessageAttributes attributes = actual.getAttributes();
        String payload = actual.payload();

        assertThat(payload).isEqualTo(fileIdToDelete);
        assertThat(attributes).containsEntry("id", fileIdToDelete);
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
        FileDeleteException thrown =
                assertThrows(FileDeleteException.class, () -> component.apply(context, message));

        // Then
        assertThat(thrown)
                .hasMessage("The File ID was null: I cannot delete a file with null ID (DynamicValue=[#[message.payload()]]).");
    }

    @Test
    void shouldThrowExceptionWhenPayloadInputIsLong() {
        // Given
        Message input = MessageBuilder.get(TestComponent.class)
                .withJavaObject(10L)
                .build();

        // When
        ComponentInputException thrown =
                assertThrows(ComponentInputException.class, () -> component.apply(context, input));

        // Then
        assertThat(thrown)
                .hasMessage("FileDelete (com.reedelk.google.drive.v3.component.FileDelete) " +
                        "was invoked with a not supported Input Type: actual=[Long], expected one of=[String,byte[]].");
    }
}
