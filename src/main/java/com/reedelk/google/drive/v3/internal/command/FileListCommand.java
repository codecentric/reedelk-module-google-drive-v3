package com.reedelk.google.drive.v3.internal.command;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import com.reedelk.google.drive.v3.internal.exception.FileListException;
import com.reedelk.google.drive.v3.internal.type.ListOfFiles;
import com.reedelk.runtime.api.exception.PlatformException;

import java.io.IOException;

import static com.reedelk.google.drive.v3.internal.commons.Messages.FileList.GENERIC_ERROR;

public class FileListCommand implements Command<ListOfFiles> {

    private final String nextPageToken;
    private final String driveId;
    private final String orderBy;
    private final String query;
    private final int pageSize;

    public FileListCommand(String driveId,
                           String orderBy,
                           int pageSize,
                           String nextPageToken,
                           String query) {
        this.nextPageToken = nextPageToken;
        this.pageSize = pageSize;
        this.driveId = driveId;
        this.orderBy = orderBy;
        this.query = query;
    }

    @Override
    public ListOfFiles execute(Drive drive) throws IOException {
        Drive.Files.List list = drive.files().list();
        list.setPageToken(nextPageToken);
        list.setPageSize(pageSize);
        list.setDriveId(driveId);
        list.setOrderBy(orderBy);
        list.setQ(query);

        FileList files = list.setFields("*").execute();

        return new ListOfFiles(files.getFiles());
    }

    @Override
    public PlatformException onException(Exception exception) {
        String error = GENERIC_ERROR.format(
                query,
                orderBy,
                driveId,
                nextPageToken,
                pageSize,
                exception.getMessage());
        return new FileListException(error, exception);
    }
}
