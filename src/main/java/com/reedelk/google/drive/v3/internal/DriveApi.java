package com.reedelk.google.drive.v3.internal;

import com.google.api.services.drive.Drive;
import com.reedelk.google.drive.v3.internal.command.Command;

import java.io.IOException;

public class DriveApi {

    private final Drive drive;

    public DriveApi(Drive drive) {
        this.drive = drive;
    }

    public <T> T execute(Command<T> command) {
        try {
            return command.execute(drive);
        } catch (IOException e) {
            throw command.onException(e);
        }
    }
}
