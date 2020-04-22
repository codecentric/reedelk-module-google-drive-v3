package com.reedelk.google.drive.v3.internal.exception;

import com.reedelk.runtime.api.exception.PlatformException;

public class PermissionDeleteException extends PlatformException {

    public PermissionDeleteException(String message) {
        super(message);
    }

    public PermissionDeleteException(String message, Throwable exception) {
        super(message, exception);
    }
}
