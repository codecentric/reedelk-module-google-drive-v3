package com.reedelk.google.drive.v3.internal.exception;

import com.reedelk.runtime.api.exception.PlatformException;

public class PermissionListException extends PlatformException {

    public PermissionListException(String message) {
        super(message);
    }

    public PermissionListException(String message, Throwable exception) {
        super(message, exception);
    }
}
