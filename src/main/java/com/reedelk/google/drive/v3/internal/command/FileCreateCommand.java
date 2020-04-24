package com.reedelk.google.drive.v3.internal.command;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.reedelk.google.drive.v3.internal.attribute.FileCreateAttributes;
import com.reedelk.google.drive.v3.internal.exception.FileCreateException;
import com.reedelk.runtime.api.exception.PlatformException;
import com.reedelk.runtime.api.message.content.MimeType;

import java.io.IOException;

import static java.lang.String.join;

public class FileCreateCommand implements Command<File> {

    private final String fileDescription;
    private final MimeType fileMimeType;
    private final boolean indexableText;
    private final byte[] fileContent;
    private final String fileName;

    public FileCreateCommand(String fileName,
                             String fileDescription,
                             MimeType fileMimeType,
                             boolean indexableText,
                             byte[] fileContent) {
        this.fileDescription = fileDescription;
        this.indexableText = indexableText;
        this.fileMimeType = fileMimeType;
        this.fileContent = fileContent;
        this.fileName = fileName;
    }

    @Override
    public File execute(Drive drive) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(fileName);
        fileMetadata.setDescription(fileDescription);

        ByteArrayContent byteArrayContent = new ByteArrayContent(fileMimeType.toString(), fileContent);

        return drive.files()
                .create(fileMetadata, byteArrayContent)
                .setUseContentAsIndexableText(indexableText)
                .setFields("*")
                .execute();
    }

    @Override
    public PlatformException onException(Exception exception) {
        String error = exception.getMessage(); // TODO: Fixme
        return new FileCreateException(error, exception);
    }
}
