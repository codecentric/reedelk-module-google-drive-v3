package com.reedelk.google.drive.v3.internal.command;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.reedelk.google.drive.v3.internal.exception.FileUpdateException;
import com.reedelk.runtime.api.exception.PlatformException;

import java.io.IOException;

public class FileUpdateCommand implements Command<File> {

    private final byte[] fileContent;
    private final String fileId;

    public FileUpdateCommand(String fileId,
                             byte[] fileContent) {
        this.fileContent = fileContent;
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
        String error = "";
        throw new FileUpdateException(error, exception);
    }
}
