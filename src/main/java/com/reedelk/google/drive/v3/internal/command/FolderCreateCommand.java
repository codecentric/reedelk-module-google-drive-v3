package com.reedelk.google.drive.v3.internal.command;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.reedelk.google.drive.v3.internal.commons.GoogleMimeTypes;
import com.reedelk.google.drive.v3.internal.exception.FolderCreateException;
import com.reedelk.runtime.api.commons.StringUtils;
import com.reedelk.runtime.api.exception.PlatformException;

import java.io.IOException;
import java.util.Collections;

public class FolderCreateCommand implements Command<File> {

    private final String folderDescription;
    private final String parentFolderId;
    private final String folderName;

    public FolderCreateCommand(String folderName,
                               String folderDescription,
                               String parentFolderId) {
        this.folderDescription = folderDescription;
        this.folderName = folderName;
        this.parentFolderId = parentFolderId;
    }

    @Override
    public File execute(Drive drive) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(folderName);
        fileMetadata.setMimeType(GoogleMimeTypes.FOLDER);
        fileMetadata.setDescription(folderDescription);

        // If given, we set the parent folder id where the folder should belong to.
        if (StringUtils.isNotBlank(parentFolderId)) {
            fileMetadata.setParents(Collections.singletonList(parentFolderId));
        }

        return drive.files()
                .create(fileMetadata)
                .setFields("*")
                .execute();
    }

    @Override
    public PlatformException onException(Exception exception) {
        String error = exception.getMessage(); // TODO: Fixme
        return new FolderCreateException(error, exception);
    }
}
