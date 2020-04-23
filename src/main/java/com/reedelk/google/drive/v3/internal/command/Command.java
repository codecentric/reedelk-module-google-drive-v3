package com.reedelk.google.drive.v3.internal.command;

import com.google.api.services.drive.Drive;
import com.reedelk.runtime.api.exception.PlatformException;

import java.io.IOException;

public interface Command<T> {

    T execute(Drive drive) throws IOException;

    PlatformException onException(Exception exception);

}
