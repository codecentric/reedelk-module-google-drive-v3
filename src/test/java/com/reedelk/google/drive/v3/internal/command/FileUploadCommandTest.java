package com.reedelk.google.drive.v3.internal.command;

import com.reedelk.runtime.api.exception.PlatformException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FileUploadCommandTest {

    @Test
    void shouldFormatErrorMessageCorrectly() {
        // Given
        FileUploadCommand command =
                new FileUploadCommand("my-file.txt", "My file description", "aabbcc", true, new byte[0]);

        // When
        PlatformException actual = command.onException(new Exception("test"));

        // Then
        assertThat(actual).hasMessage("Could not upload file on Google Drive with configuration fileName=[my-file.txt], " +
                "fileDescription=[My file description], indexableText=[true], parentFolderId=[aabbcc], cause=[test].");
    }
}