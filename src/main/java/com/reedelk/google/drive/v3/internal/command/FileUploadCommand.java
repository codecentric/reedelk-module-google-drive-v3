package com.reedelk.google.drive.v3.internal.command;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.reedelk.google.drive.v3.internal.attribute.FileUploadAttributes;
import com.reedelk.google.drive.v3.internal.exception.FileUploadException;
import com.reedelk.runtime.api.exception.PlatformException;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static com.reedelk.google.drive.v3.internal.commons.Messages.FileUpload.GENERIC_ERROR;
import static com.reedelk.runtime.api.commons.StringUtils.isNotBlank;
import static java.lang.String.join;

public class FileUploadCommand implements Command<File> {

    private final String fileDescription;
    private final boolean indexableText;
    private final String parentFolderId;
    private final String fileName;
    private final byte[] fileContent;

    public FileUploadCommand(String fileName,
                             String fileDescription,
                             String parentFolderId,
                             boolean indexableText,
                             byte[] fileContent) {
        this.fileDescription = fileDescription;
        this.parentFolderId = parentFolderId;
        this.indexableText = indexableText;
        this.fileName = fileName;

        this.fileContent = Optional.ofNullable(fileContent).orElse(new byte[0]);
    }

    @Override
    public File execute(Drive drive) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(fileName);
        fileMetadata.setDescription(fileDescription);

        // If given, we set the parent folder id where the file should belong to.
        if (isNotBlank(parentFolderId)) {
            fileMetadata.setParents(Collections.singletonList(parentFolderId));
        }

        ByteArrayContent byteArrayContent = new ByteArrayContent(null, fileContent);

        return drive.files()
                .create(fileMetadata, byteArrayContent)
                .setUseContentAsIndexableText(indexableText)
                .setFields(join(",", FileUploadAttributes.ALL_ATTRIBUTES))
                .execute();
    }

    @Override
    public PlatformException onException(Exception exception) {
        String error = GENERIC_ERROR.format(
                fileName,
                fileDescription,
                indexableText,
                parentFolderId,
                exception.getMessage());
        return new FileUploadException(error, exception);
    }
}
