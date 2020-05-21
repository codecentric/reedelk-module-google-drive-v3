package com.reedelk.google.drive.v3.internal.type;

import com.google.api.services.drive.model.File;
import com.reedelk.runtime.api.annotation.Type;
import com.reedelk.runtime.api.annotation.TypeProperty;

import java.io.Serializable;
import java.util.HashMap;

import static com.reedelk.google.drive.v3.internal.type.FileType.*;

@Type(displayName = "FileType", mapKeyType = String.class, mapValueType = Serializable.class)
@TypeProperty(name = ID, type = String.class)
@TypeProperty(name = NAME, type = String.class)
@TypeProperty(name = KIND, type = String.class)
@TypeProperty(name = DRIVE_ID, type = String.class)
@TypeProperty(name = OWNED_BY_ME, type = Boolean.class)
@TypeProperty(name = WEB_VIEW_LINK, type = String.class)
@TypeProperty(name = WEB_CONTENT_LINK, type = String.class)
@TypeProperty(name = DESCRIPTION, type = String.class)
@TypeProperty(name = FILE_EXTENSION, type = String.class)
@TypeProperty(name = ORIGINAL_FILE_NAME, type = String.class)
@TypeProperty(name = OWNERS, type = ListOfUsers.class)
public class FileType extends HashMap<String, Serializable> {

    static final String ID = "id";
    static final String NAME = "name";
    static final String KIND = "kind";
    static final String DRIVE_ID = "driveId";
    static final String OWNED_BY_ME = "ownedByMe";
    static final String WEB_VIEW_LINK = "webViewLink";
    static final String WEB_CONTENT_LINK = "webContentLink";
    static final String DESCRIPTION = "description";
    static final String FILE_EXTENSION = "fileExtension";
    static final String ORIGINAL_FILE_NAME = "originalFilename";
    static final String OWNERS = "owners";

    public FileType(File file) {
        put(ID, file.getId());
        put(NAME, file.getName());
        put(KIND, file.getKind());
        put(DRIVE_ID, file.getDriveId());
        put(OWNED_BY_ME, file.getOwnedByMe());
        put(WEB_VIEW_LINK, file.getWebViewLink());
        put(DESCRIPTION, file.getDescription());
        put(FILE_EXTENSION, file.getFileExtension());
        put(WEB_CONTENT_LINK, file.getWebContentLink());
        put(ORIGINAL_FILE_NAME, file.getOriginalFilename());
        put(OWNERS, new ListOfUsers(file.getOwners()));
    }
}
