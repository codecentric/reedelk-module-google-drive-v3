package com.reedelk.google.drive.internal.exception;

import com.reedelk.runtime.api.exception.PlatformException;

public class PermissionCreateException extends PlatformException {

    public PermissionCreateException(String message) {
        super(message);
    }

    public PermissionCreateException(String message, Throwable exception) {
        super(message, exception);
    }
}
