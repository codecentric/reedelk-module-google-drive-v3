package com.reedelk.google.drive.v3.internal.type;

import com.google.api.services.drive.model.File;
import com.reedelk.runtime.api.annotation.Type;

import java.util.ArrayList;
import java.util.List;

@Type(listItemType = FileType.class)
public class ListOfFiles extends ArrayList<FileType> {

    public ListOfFiles(List<File> files) {
        if (files != null) {
            files.stream().map(FileType::new).forEach(this::add);
        }
    }
}
