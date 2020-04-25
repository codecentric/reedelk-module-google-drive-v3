package com.reedelk.google.drive.v3.internal.command;

import com.reedelk.runtime.api.exception.PlatformException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FileListCommandTest {

    @Test
    void shouldFormatErrorMessageCorrectly() {
        // Given
        FileListCommand command =
                new FileListCommand("my-drive-id", "name,description", 21, "aabb", "name = 'test'");

        // When
        PlatformException actual = command.onException(new Exception("test"));

        // Then
        assertThat(actual).hasMessage("Could not list files from Google Drive with configuration query=[name = 'test'], " +
                "orderBy=[name,description], driveId=[my-drive-id], nextPageToken=[aabb], " +
                "pageSize=[21], cause=[test].");
    }
}