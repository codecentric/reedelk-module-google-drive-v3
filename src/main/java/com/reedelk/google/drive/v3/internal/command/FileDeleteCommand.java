package com.reedelk.google.drive.v3.internal.command;

import com.google.api.services.drive.Drive;
import com.reedelk.google.drive.v3.internal.exception.FileDeleteException;
import com.reedelk.runtime.api.exception.PlatformException;

import java.io.IOException;

import static com.reedelk.google.drive.v3.internal.commons.Messages.FileCreate.GENERIC_ERROR;

public class FileDeleteCommand implements Command<Void> {

    private final String fileId;

    public FileDeleteCommand(String fileId) {
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
