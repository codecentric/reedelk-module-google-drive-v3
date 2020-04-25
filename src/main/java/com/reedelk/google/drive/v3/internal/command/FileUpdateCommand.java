package com.reedelk.google.drive.v3.internal.command;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.reedelk.google.drive.v3.internal.exception.FileDownloadException;
import com.reedelk.google.drive.v3.internal.exception.FileUpdateException;
import com.reedelk.runtime.api.exception.PlatformException;

import java.io.IOException;
import java.util.Optional;

import static com.reedelk.google.drive.v3.internal.commons.Messages.FileUpdate.FILE_ID_EMPTY;
import static com.reedelk.google.drive.v3.internal.commons.Messages.FileUpdate.GENERIC_ERROR;
import static com.reedelk.runtime.api.commons.StringUtils.isBlank;

public class FileUpdateCommand implements Command<File> {

    private final byte[] fileContent;
    private final String fileId;

    public FileUpdateCommand(String fileId, byte[] fileContent) {
        if (isBlank(fileId)) {
            throw new FileDownloadException(FILE_ID_EMPTY.format(fileId));
        }
        this.fileContent = Optional.ofNullable(fileContent).orElse(new byte[0]);
        this.fileId = fileId;
    }

    @Override
    public File execute(Drive drive) throws IOException {
        ByteArrayContent byteArrayContent = new ByteArrayContent(null ,fileContent);

        return drive.files()
                .update(fileId, null, byteArrayContent)
                .execute();
    }

    @Override
    public PlatformException onException(Exception exception) {
        String error = GENERIC_ERROR.format(fileId, exception.getMessage());
        throw new FileUpdateException(error, exception);
    }
}
