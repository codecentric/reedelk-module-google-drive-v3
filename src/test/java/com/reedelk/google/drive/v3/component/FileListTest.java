package com.reedelk.google.drive.v3.component;

import com.reedelk.google.drive.v3.internal.command.FileListCommand;
import com.reedelk.google.drive.v3.internal.type.ListOfFiles;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageAttributes;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.message.content.TypedContent;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import java.util.ArrayList;
import java.util.UUID;

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
    void shouldInvokeCommandWithCorrectComponentConfigurationArguments() {
        // Given
        String nextPageToken = UUID.randomUUID().toString();
        String orderBy = "folder,modifiedTime desc";
        String driveId = "my-drive-test";
        String query = "name = 'hello'";
        int pageSize = 35;

        component.setNextPageToken(DynamicString.from(nextPageToken));
        component.setQuery(DynamicString.from(query));
        component.setPageSize(pageSize);
        component.setDriveId(driveId);
        component.setOrderBy(orderBy);
        component.initialize();

        // When
        component.apply(context, message);

        // Then
        verify(driveApi).execute(captor.capture());
        FileListCommand value = captor.getValue();

        assertThat(value).hasFieldOrPropertyWithValue("nextPageToken", nextPageToken);
        assertThat(value).hasFieldOrPropertyWithValue("pageSize", pageSize);
        assertThat(value).hasFieldOrPropertyWithValue("driveId", driveId);
        assertThat(value).hasFieldOrPropertyWithValue("orderBy", orderBy);
        assertThat(value).hasFieldOrPropertyWithValue("query", query);
    }

    @Test
    void shouldInvokeCommandWithCorrectDefaultArguments() {
        // Given
        component.initialize();

        // When
        component.apply(context, message);

        // Then
        verify(driveApi).execute(captor.capture());
        FileListCommand value = captor.getValue();

        assertThat(value).hasFieldOrPropertyWithValue("nextPageToken", null);
        assertThat(value).hasFieldOrPropertyWithValue("pageSize", 100);
        assertThat(value).hasFieldOrPropertyWithValue("driveId", null);
        assertThat(value).hasFieldOrPropertyWithValue("orderBy", null);
        assertThat(value).hasFieldOrPropertyWithValue("query", null);
    }

    @Test
    void shouldReturnCorrectPayloadWithDefaultMimeTypeAndAttributes() {
        // Given
        component.initialize();

        ListOfFiles mappedFiles = new ListOfFiles(new ArrayList<>());

        doReturn(mappedFiles)
                .when(driveApi)
                .execute(any(FileListCommand.class));

        // When
        Message actual = component.apply(context, message);

        // Then
        ListOfFiles payload = actual.payload();
        assertThat(payload).isEqualTo(mappedFiles);

        MessageAttributes attributes = actual.getAttributes();
        assertThat(attributes).isNotNull();

        TypedContent<Object, Object> content = actual.getContent();
        assertThat(content.getMimeType()).isEqualTo(MimeType.APPLICATION_JAVA);
    }
}
