package com.reedelk.google.drive.v3.internal.exception;

import com.reedelk.runtime.api.exception.PlatformException;

public class PermissionUpdateException extends PlatformException {

    public PermissionUpdateException(String message) {
        super(message);
    }

    public PermissionUpdateException(String message, Throwable exception) {
        super(message, exception);
    }
}
