package com.reedelk.google.drive.v3.component;

import com.reedelk.google.drive.v3.internal.command.FileListCommand;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class FileListTest extends AbstractComponentTest {

    @Captor
    protected ArgumentCaptor<FileListCommand> captor = ArgumentCaptor.forClass(FileListCommand.class);

    private FileList component = spy(new FileList());

    @BeforeEach
    void setUp() {
        super.setUp();
        component.scriptEngine = scriptEngine;
        doReturn(driveApi).when(component).createApi();
    }

    @Test
    void shouldInvokeCommandWithCorrectArgumentsWhenFileIdPropertyIsPresent() {
        // Given
        String query = "name = 'hello'";
        component.setQuery(DynamicString.from(query));
        component.initialize();

        // When
        component.apply(context, message);

        // Then
        verify(driveApi).execute(captor.capture());
        FileListCommand value = captor.getValue();

        assertThat(value).hasFieldOrPropertyWithValue("query", query);
        assertThat(value).hasFieldOrPropertyWithValue("pageSize", 100);
    }
}