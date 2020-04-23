package com.reedelk.google.drive.v3.internal;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.reedelk.google.drive.v3.internal.exception.FileCreateException;
import com.reedelk.runtime.api.message.content.MimeType;

import java.io.IOException;

import static com.reedelk.google.drive.v3.internal.commons.Messages.FileCreate.GENERIC_ERROR;
import static java.lang.String.join;

public class DriveApiImpl implements DriveApi {

    private final Drive drive;

    public DriveApiImpl(Drive drive) {
        this.drive = drive;
    }

    @Override
    public File fileCreate(String fileName,
                           String fileDescription,
                           MimeType fileMimeType,
                           boolean indexableText,
                           byte[] fileContent) {
        File fileMetadata = new File();
        fileMetadata.setName(fileName);
        fileMetadata.setDescription(fileDescription);
        ByteArrayContent byteArrayContent = new ByteArrayContent(fileMimeType.toString(), fileContent);
        try {
            return drive.files()
                    .create(fileMetadata, byteArrayContent)
                    .setUseContentAsIndexableText(indexableText)
                    .setFields(join(",", FileCreateAttributes.ALL_ATTRIBUTES))
                    .execute();
        } catch (IOException exception) {
            String error = GENERIC_ERROR.format(fileName, exception.getMessage());
            throw new FileCreateException(error, exception);
        }
    }
}
