package com.reedelk.google.drive.v3.internal.commons;

import com.reedelk.runtime.api.commons.FormattedMessage;

public class Messages {

    private Messages() {
    }

    public enum FileDelete implements FormattedMessage {

        GENERIC_ERROR("The file with File ID=[%s] could not be deleted on Google Drive, cause=[%s]."),
        FILE_ID_EMPTY("The File ID was empty: I cannot delete a file from an empty File ID."),
        FILE_ID_NULL("The File ID was null: I cannot delete a file with null ID (DynamicValue=[%s]).");

        private final String message;

        FileDelete(String message) {
            this.message = message;
        }

        @Override
        public String template() {
            return message;
        }
    }

    public enum FileDownload implements FormattedMessage {

        GENERIC_ERROR("The file with File ID=[%s] could not be downloaded from Google Drive, cause=[%s]."),
        FILE_ID_EMPTY("The File ID was empty: I cannot download a file from an empty File ID."),
        FILE_ID_NULL("The File ID was null: I cannot download a file with null ID (DynamicValue=[%s]).");

        private final String message;

        FileDownload(String message) {
            this.message = message;
        }

        @Override
        public String template() {
            return message;
        }
    }

    public enum FileList implements FormattedMessage {

        GENERIC_ERROR("Could not list files from Google Drive with configuration " +
                "query=[%s], orderBy=[%s], driveId=[%s], nextPageToken=[%s], pageSize=[%d], cause=[%s].");

        private final String message;

        FileList(String message) {
            this.message = message;
        }

        @Override
        public String template() {
            return message;
        }
    }

    public enum FileUpdate implements FormattedMessage {

        FILE_ID_NULL("The File ID was null: I cannot update a file with null ID (DynamicValue=[%s])."),
        FILE_ID_EMPTY("The File ID was empty: I cannot update a file from an empty File ID.");

        private final String message;

        FileUpdate(String message) {
            this.message = message;
        }

        @Override
        public String template() {
            return message;
        }
    }

    public enum PermissionList implements FormattedMessage {

        GENERIC_ERROR("Could not list permissions from Google Drive for File ID=[%s], cause=[%s].");

        private final String message;

        PermissionList(String message) {
            this.message = message;
        }

        @Override
        public String template() {
            return message;
        }
    }

    public enum Misc implements FormattedMessage {

        DRIVE_CREDENTIALS_ERROR_GENERIC("Could not create Google Drive Credentials, cause=[%s]"),
        DRIVE_CREDENTIALS_ERROR("Could not create Google Drive Credentials from file path=[%s], cause=[%s]");

        private final String message;

        Misc(String message) {
            this.message = message;
        }

        @Override
        public String template() {
            return message;
        }
    }
}
