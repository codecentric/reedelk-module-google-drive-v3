package com.reedelk.google.drive.v3.internal.commons;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.model.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

public class Mappers {

    private static final Function<User, Map<String, Serializable>> USER = user -> {
        Map<String, Serializable> map = new HashMap<>();
        map.put("emailAddress", user.getEmailAddress());
        return map;
    };

    public static class FromFile {

        private static final String FIELD_ID = "id";
        private static final String FIELD_NAME = "name";
        private static final String FIELD_KIND = "kind";
        private static final String FIELD_DRIVE_ID = "driveId";
        private static final String FIELD_OWNED_BY_ME = "ownedByMe";
        private static final String FIELD_WEB_VIEW_LINK = "webViewLink";
        private static final String FIELD_WEB_CONTENT_LINK = "webContentLink";
        private static final String FIELD_DESCRIPTION = "description";
        private static final String FIELD_FILE_EXTENSION = "fileExtension";
        private static final String FIELD_ORIGINAL_FILE_NAME = "originalFilename";
        private static final String FIELD_OWNERS = "owners";

        public static final List<String> FIELDS = unmodifiableList(asList(
                FIELD_ID,
                FIELD_NAME,
                FIELD_KIND,
                FIELD_DRIVE_ID,
                FIELD_OWNED_BY_ME,
                FIELD_WEB_VIEW_LINK,
                FIELD_WEB_CONTENT_LINK,
                FIELD_DESCRIPTION,
                FIELD_FILE_EXTENSION,
                FIELD_ORIGINAL_FILE_NAME,
                FIELD_OWNERS));

        public static final Function<File, Map<String, Serializable>> GET = file -> {
            Map<String, Serializable> map = new HashMap<>();
            map.put(FIELD_ID, file.getId());
            map.put(FIELD_NAME, file.getName());
            map.put(FIELD_KIND, file.getKind());
            map.put(FIELD_DRIVE_ID, file.getDriveId());
            map.put(FIELD_OWNED_BY_ME, file.getOwnedByMe());
            map.put(FIELD_WEB_VIEW_LINK, file.getWebViewLink());
            map.put(FIELD_DESCRIPTION, file.getDescription());
            map.put(FIELD_FILE_EXTENSION, file.getFileExtension());
            map.put(FIELD_WEB_CONTENT_LINK, file.getWebContentLink());
            map.put(FIELD_ORIGINAL_FILE_NAME, file.getOriginalFilename());
            if (file.getOwners() != null) {
                List<Map<String, Serializable>> collect =
                        file.getOwners().stream().map(USER).collect(toList());
                map.put(FIELD_OWNERS, new ArrayList<>(collect));
            }
            return map;
        };
    }

    public static final Function<Permission, Map<String, Serializable>> PERMISSION = permission -> {
        Map<String, Serializable> map = new HashMap<>();
        map.put("id", permission.getId());
        map.put("type", permission.getType());
        map.put("role", permission.getRole());
        map.put("domain", permission.getDomain());
        map.put("displayName", permission.getDomain());
        map.put("emailAddress", permission.getEmailAddress());
        return map;
    };
}
