package com.reedelk.google.drive.v3.internal.commons;

import com.google.api.services.drive.model.Permission;
import com.reedelk.google.drive.v3.component.PermissionCreate;
import com.reedelk.google.drive.v3.component.PermissionRole;
import com.reedelk.google.drive.v3.component.PermissionType;
import com.reedelk.runtime.api.exception.PlatformException;
import com.reedelk.runtime.api.flow.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.ScriptEngineService;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;

import static com.reedelk.runtime.api.commons.ConfigurationPreconditions.requireNotNullOrBlank;

public class PermissionUtils {

    public static Permission createPermission(ScriptEngineService scriptEngine,
                                        DynamicString emailAddress,
                                        DynamicString domain,
                                        PermissionRole role,
                                        PermissionType type,
                                        FlowContext flowContext,
                                        Message message) {
        Permission userPermission = new Permission();
        role.set(userPermission);
        type.set(userPermission);

        if (PermissionType.USER.equals(type) || PermissionType.GROUP.equals(type)) {
            // emailAddress is mandatory (see Google Doc: https://developers.google.com/drive/api/v3/reference/permissions/create).
            String evaluatedEmailAddress = scriptEngine.evaluate(emailAddress, flowContext, message)
                    .orElseThrow(() -> new PlatformException("Email address is mandatory when permission type is user or group"));
            userPermission.setEmailAddress(evaluatedEmailAddress);

        } else if (PermissionType.DOMAIN.equals(type)) {
            // domain is mandatory (see Google Doc: https://developers.google.com/drive/api/v3/reference/permissions/create).
            String evaluatedDomain = scriptEngine.evaluate(domain, flowContext, message)
                    .orElseThrow(() -> new PlatformException("Domain is mandatory when permission type is user or group"));
            userPermission.setDomain(evaluatedDomain);
        }
        return userPermission;
    }

    public static void checkPreconditions(PermissionRole role, PermissionType type, DynamicString emailAddress, DynamicString domain) {
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
