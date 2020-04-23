package com.reedelk.google.drive.v3.internal;

import com.google.api.services.drive.model.File;
import com.reedelk.runtime.api.message.content.MimeType;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface DriveApi {

    File fileCreate(String fileName,
                    String fileDescription,
                    MimeType fileMimeType,
                    boolean indexableText,
                    byte[] fileContent);

    List<Map<String, Serializable>> fileList(String driveId,
                                             String orderBy,
                                             String query,
                                             String nextPageToken,
                                             int pageSize);

    void fileDelete(String fileId);

    byte[] fileRead(String fileId);

    void fileUpdate(String realFileId,
                    MimeType fileMimeType,
                    byte[] fileContent);
}
