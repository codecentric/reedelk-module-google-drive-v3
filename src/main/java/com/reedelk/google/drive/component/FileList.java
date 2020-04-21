package com.reedelk.google.drive.component;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.User;
import com.reedelk.google.drive.internal.DriveServiceProvider;
import com.reedelk.runtime.api.annotation.ModuleComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.flow.FlowContext;
import com.reedelk.runtime.api.message.Message;
import org.osgi.service.component.annotations.Component;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

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
        this.drive = provider.get(FileCreate.class, configuration);

        // Add in the config precondition this stuf... DynamicValue and such.
        //DynamicValueUtils.isNullOrBlank()
    }


    @Override
    public Message apply(FlowContext flowContext, Message message) {

        try {
            com.google.api.services.drive.model.FileList files = drive.files().list()
                    .execute();
            files.getFiles().forEach(new Consumer<File>() {
                @Override
                public void accept(File file) {
                    String driveId = file.getDriveId();
                    String fileExtension = file.getFileExtension();
                    String id = file.getId();
                    String description = file.getDescription();
                    String name = file.getName();
                    String originalFilename = file.getOriginalFilename();
                    Boolean ownedByMe = file.getOwnedByMe();
                    List<User> owners = file.getOwners();
                    File.Capabilities capabilities = file.getCapabilities();
                    String webViewLink = file.getWebViewLink();
                    String webContentLink = file.getWebContentLink();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
