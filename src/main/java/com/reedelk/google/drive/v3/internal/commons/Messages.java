package com.reedelk.google.drive.v3.internal.commons;

import com.reedelk.runtime.api.commons.FormattedMessage;

public class Messages {

    private Messages() {
    }

    public enum FileCreate implements FormattedMessage {

        FILE_NAME_EMPTY("The file name to be created on Google Drive must not be empty (DynamicValue=[%s])."),
        GENERIC_ERROR("The file with name=[%s] could not be created on Google Drive, cause=[%s]");


        private String message;

        FileCreate(String message) {
            this.message = message;
        }

        @Override
        public String template() {
            return message;
        }
    }

    public enum FileRead implements FormattedMessage {
        ;

        @Override
        public String template() {
            return null;
        }
    }

    public enum FileDelete implements FormattedMessage {

        FILE_ID_EMPTY("The File ID was empty: I cannot delete a file with empty ID."),
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
