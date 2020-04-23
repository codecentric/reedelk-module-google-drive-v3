package com.reedelk.google.drive.v3.internal;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.reedelk.google.drive.v3.internal.commons.Mappers;
import com.reedelk.google.drive.v3.internal.exception.FileCreateException;
import com.reedelk.google.drive.v3.internal.exception.FileDeleteException;
import com.reedelk.google.drive.v3.internal.exception.FileListException;
import com.reedelk.runtime.api.message.content.MimeType;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static com.reedelk.google.drive.v3.internal.commons.Messages.FileCreate.GENERIC_ERROR;
import static java.lang.String.join;
import static java.util.stream.Collectors.toList;

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
            // TODO: Should not use generic errors ?
            String error = GENERIC_ERROR.format(fileName, exception.getMessage());
            throw new FileCreateException(error, exception);
        }
    }

    @Override
    public List<Map<String, Serializable>> fileList(String driveId,
                                                    String orderBy,
                                                    String query,
                                                    String nextPageToken,
                                                    int pageSize) {
        try {
            Drive.Files.List list = drive.files().list();
            list.setPageToken(nextPageToken);
            list.setPageSize(pageSize);
            list.setDriveId(driveId);
            list.setOrderBy(orderBy);
            list.setQ(query);

            FileList files = list.setFields("*").execute();

            return files.getFiles()
                    .stream()
                    .map(Mappers.FILE)
                    .collect(toList());

        } catch (IOException e) {
            String error = "";
            throw new FileListException(error, e);
        }
    }

    @Override
    public void fileDelete(String realFileId) {
        try {
            drive.files().delete(realFileId).execute();
        } catch (IOException exception) {
            // TODO: Should not use generic errors ?
            String error = GENERIC_ERROR.format(realFileId, exception.getMessage());
            throw new FileDeleteException(error, exception);
        }
    }
}
