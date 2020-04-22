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

import static java.util.stream.Collectors.toList;

public class Mappers {

    private static final Function<User, Map<String, Serializable>> USER = user -> {
        Map<String, Serializable> map = new HashMap<>();
        map.put("emailAddress", user.getEmailAddress());
        return map;
    };

    public static final Function<File, Map<String, Serializable>> FILE = file -> {
        Map<String, Serializable> map = new HashMap<>();
        map.put("driveId", file.getDriveId());
        map.put("fileExtension", file.getFileExtension());
        map.put("id", file.getId());
        map.put("description", file.getDescription());
        map.put("name", file.getName());
        map.put("originalFilename", file.getOriginalFilename());
        map.put("ownedByMe", file.getOwnedByMe());

        List<Map<String, Serializable>> collect = file.getOwners().stream().map(USER).collect(toList());
        map.put("owners", new ArrayList<>(collect));
        map.put("webViewLink", file.getWebViewLink());
        map.put("webContentLink", file.getWebContentLink());
        return map;
    };

    public static final Function<Permission, Map<String, Serializable>> PERMISSION = permission -> {
        Map<String, Serializable> map = new HashMap<>();
        map.put("id", permission.getId());
        map.put("emailAddress", permission.getEmailAddress());
        map.put("type", permission.getType());
        map.put("role", permission.getRole());
        map.put("domain", permission.getDomain());
        map.put("displayName", permission.getDomain());
        return map;
    };
}
