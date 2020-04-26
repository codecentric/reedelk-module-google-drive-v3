package com.reedelk.google.drive.v3.internal.commons;

import com.reedelk.google.drive.v3.component.PermissionCreate;
import com.reedelk.google.drive.v3.component.PermissionType;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;

import static com.reedelk.runtime.api.commons.ComponentPrecondition.Configuration.requireNotNullOrBlank;

public class PermissionUtils {

    public static void checkPreconditions(PermissionType type, DynamicString emailAddress, DynamicString domain) {
        if (PermissionType.USER.equals(type) || PermissionType.GROUP.equals(type)) {
            requireNotNullOrBlank(PermissionCreate.class, emailAddress,
                    "Email Address must not be empty when permission type is user or group.");
        }
        if (PermissionType.DOMAIN.equals(type)) {
            requireNotNullOrBlank(PermissionCreate.class, domain,
                    "Domain must not be empty when permission type is domain.");
        }
    }
}
