package com.reedelk.google.drive.v3.internal.command;

import com.google.api.services.drive.Drive;
import com.reedelk.google.drive.v3.internal.exception.FileDownloadException;
import com.reedelk.runtime.api.exception.PlatformException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.reedelk.google.drive.v3.internal.commons.Messages.FileDownload.FILE_ID_EMPTY;
import static com.reedelk.google.drive.v3.internal.commons.Messages.FileDownload.GENERIC_ERROR;
import static com.reedelk.runtime.api.commons.StringUtils.isBlank;

public class FileDownloadCommand implements Command<byte[]> {

    private final String fileId;

    public FileDownloadCommand(String fileId) {
        if (isBlank(fileId)) {
            throw new FileDownloadException(FILE_ID_EMPTY.format());
        }
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
        String error = GENERIC_ERROR.format(fileId, exception.getMessage());
        throw new FileDownloadException(error, exception);
    }
}
