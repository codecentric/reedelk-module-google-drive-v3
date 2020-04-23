package com.reedelk.google.drive.v3.internal.command;

import com.google.api.services.drive.Drive;
import com.reedelk.google.drive.v3.internal.exception.FileReadException;
import com.reedelk.runtime.api.exception.PlatformException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class FileReadCommand implements Command<byte[]> {

    private final String fileId;

    public FileReadCommand(String fileId) {
        this.fileId = fileId;
    }

    @Override
    public byte[] execute(Drive drive) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            drive.files()
                    .get(fileId)
                    .executeMediaAndDownloadTo(outputStream);
            return outputStream.toByteArray();
        }
    }

    @Override
    public PlatformException onException(Exception exception) {
        String error = ""; // TODO: Fixme.
        throw new FileReadException(error, exception);
    }
}
