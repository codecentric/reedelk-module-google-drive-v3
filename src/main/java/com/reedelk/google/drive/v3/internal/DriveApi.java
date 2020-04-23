package com.reedelk.google.drive.v3.internal;

import com.google.api.services.drive.model.File;
import com.reedelk.runtime.api.message.content.MimeType;

public interface DriveApi {

    File fileCreate(String fileName,
                    String fileDescription,
                    MimeType fileMimeType,
                    boolean indexableText,
                    byte[] fileContent);
}
