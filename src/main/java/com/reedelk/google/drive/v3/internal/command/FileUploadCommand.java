package com.reedelk.google.drive.v3.internal.command;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.reedelk.google.drive.v3.internal.exception.FileUploadException;
import com.reedelk.runtime.api.commons.StringUtils;
import com.reedelk.runtime.api.exception.PlatformException;

import java.io.IOException;
import java.util.Collections;

public class FileUploadCommand implements Command<File> {

    private final String fileDescription;
    private final boolean indexableText;
    private final String parentFolderId;
    private final byte[] fileContent;
    private final String fileName;

    public FileUploadCommand(String fileName,
                             String fileDescription,
                             String parentFolderId,
                             boolean indexableText,
                             byte[] fileContent) {
        this.fileDescription = fileDescription;
        this.indexableText = indexableText;
        this.fileContent = fileContent;
        this.fileName = fileName;
        this.parentFolderId = parentFolderId;
    }

    @Override
    public File execute(Drive drive) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(fileName);
        fileMetadata.setDescription(fileDescription);

        // If given, we set the parent folder id where the file should belong to.
        if (StringUtils.isNotBlank(parentFolderId)) {
            fileMetadata.setParents(Collections.singletonList(parentFolderId));
        }

        ByteArrayContent byteArrayContent = new ByteArrayContent(null, fileContent);

        return drive.files()
                .create(fileMetadata, byteArrayContent)
                .setUseContentAsIndexableText(indexableText)
                .setFields("*")
                .execute();
    }

    @Override
    public PlatformException onException(Exception exception) {
        String error = exception.getMessage(); // TODO: Fixme
        return new FileUploadException(error, exception);
    }
}
