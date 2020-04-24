package com.reedelk.google.drive.v3.internal.command;

import com.google.api.services.drive.Drive;
import com.reedelk.google.drive.v3.internal.exception.FileDeleteException;
import com.reedelk.runtime.api.exception.PlatformException;

import java.io.IOException;

import static com.reedelk.google.drive.v3.internal.commons.Messages.FileCreate.GENERIC_ERROR;
import static com.reedelk.google.drive.v3.internal.commons.Messages.FileDelete.FILE_ID_EMPTY;
import static com.reedelk.runtime.api.commons.StringUtils.isBlank;

public class FileDeleteCommand implements Command<Void> {

    private final String fileId;

    public FileDeleteCommand(String fileId) {
        if (isBlank(fileId)) throw new FileDeleteException(FILE_ID_EMPTY.format(fileId));
        this.fileId = fileId;
    }

    @Override
    public Void execute(Drive drive) throws IOException {
        drive.files()
                .delete(fileId)
                .execute();
        return null;
    }

    @Override
    public PlatformException onException(Exception exception) {
        String error = GENERIC_ERROR.format(fileId, exception.getMessage());
        throw new FileDeleteException(error, exception);
    }
}
