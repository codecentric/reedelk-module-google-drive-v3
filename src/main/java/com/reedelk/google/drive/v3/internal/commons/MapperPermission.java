package com.reedelk.google.drive.v3.internal.commons;

import com.google.api.services.drive.model.Permission;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

public class MapperPermission {

    private static final String FIELD_ID = "id";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_ROLE = "role";
    private static final String FIELD_DOMAIN = "domain";
    private static final String FIELD_DISPLAY_NAME = "displayName";
    private static final String FIELD_EMAIL_ADDRESS = "emailAddress";

    public static final List<String> FIELDS = unmodifiableList(asList(
            FIELD_ID,
            FIELD_TYPE,
            FIELD_ROLE,
            FIELD_DOMAIN,
            FIELD_DISPLAY_NAME,
            FIELD_EMAIL_ADDRESS));

    public static final Function<Permission, Map<String, Serializable>> GET = permission -> {
        Map<String, Serializable> map = new HashMap<>();
        map.put(FIELD_ID, permission.getId());
        map.put(FIELD_TYPE, permission.getType());
        map.put(FIELD_ROLE, permission.getRole());
        map.put(FIELD_DOMAIN, permission.getDomain());
        map.put(FIELD_DISPLAY_NAME, permission.getDomain());
        map.put(FIELD_EMAIL_ADDRESS, permission.getEmailAddress());
        return map;
    };
}
