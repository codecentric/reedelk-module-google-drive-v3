package com.reedelk.google.drive.v3.internal.exception;

import com.reedelk.runtime.api.exception.PlatformException;
import com.reedelk.runtime.api.message.Message;

public class FileReadException extends PlatformException {

    public FileReadException(Message message) {
        super(message);
    }

    public FileReadException(String message, Throwable exception) {
        super(message, exception);
    }
}
