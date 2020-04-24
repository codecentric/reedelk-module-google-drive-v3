package com.reedelk.google.drive.v3.component;

import com.reedelk.google.drive.v3.internal.command.FileDeleteCommand;
import com.reedelk.google.drive.v3.internal.exception.FileDeleteException;
import com.reedelk.runtime.api.commons.ModuleContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageAttributes;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;


class FileDeleteTest extends AbstractComponentTest {

    private FileDelete component = new FileDelete();

    @BeforeEach
    void setUp() {
        super.setUp();
        component.driveApi = driveApi;
        component.scriptEngine = scriptEngine;
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
        assertThat(attributes).containsEntry("fileId", fileIdToDelete);
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
}
