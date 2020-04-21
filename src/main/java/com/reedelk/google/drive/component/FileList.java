package com.reedelk.google.drive.component;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.User;
import com.reedelk.google.drive.internal.DriveServiceProvider;
import com.reedelk.runtime.api.annotation.ModuleComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.exception.PlatformException;
import com.reedelk.runtime.api.flow.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.osgi.service.component.annotations.Component;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ModuleComponent("Google File List")
@Component(service = FileList.class, scope = PROTOTYPE)
public class FileList implements ProcessorSync {

    @Property("Configuration")
    private DriveConfiguration configuration;
    private Drive drive;

    @Override
    public void initialize() {
        DriveServiceProvider provider = new DriveServiceProvider();
        this.drive = provider.get(FileList.class, configuration);
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {
        try {
            com.google.api.services.drive.model.FileList files = drive.files().list()
                    .setFields("*")
                    .execute();
            List<Map<String, Serializable>> mappedFiles = files.getFiles()
                    .stream()
                    .map(FILE_MAPPER)
                    .collect(toList());

            return MessageBuilder.get(FileList.class)
                    .withJavaObject(mappedFiles)
                    .build();
        } catch (IOException exception) {
            throw new PlatformException(exception);
        }
    }

    public void setConfiguration(DriveConfiguration configuration) {
        this.configuration = configuration;
    }

    private final Function<File, Map<String, Serializable>> FILE_MAPPER = new Function<File, Map<String, Serializable>>() {
        @Override
        public Map<String, Serializable> apply(File file) {
            Map<String, Serializable> map = new HashMap<>();
            map.put("driveId", file.getDriveId());
            map.put("fileExtension", file.getFileExtension());
            map.put("id", file.getId());
            map.put("description", file.getDescription());
            map.put("name", file.getName());
            map.put("originalFilename", file.getOriginalFilename());
            map.put("ownedByMe", file.getOwnedByMe());

            List<Map<String, Serializable>> collect = file.getOwners().stream().map(USER_MAPPER).collect(toList());
            map.put("owners", new ArrayList<>(collect));
            map.put("webViewLink", file.getWebViewLink());
            map.put("webContentLink", file.getWebContentLink());
            return map;
        }
    };

    private final Function<User, Map<String, Serializable>> USER_MAPPER = new Function<User, Map<String, Serializable>>() {
        @Override
        public Map<String, Serializable> apply(User user) {
            Map<String, Serializable> map = new HashMap<>();
            map.put("emailAddress", user.getEmailAddress());
            return map;
        }
    };
}
