package com.reedelk.google.drive.v3.component;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.Implementor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

@Shared
@Component(service = DriveConfiguration.class, scope = ServiceScope.PROTOTYPE)
public class DriveConfiguration implements Implementor {

    @WidthAuto
    @Mandatory
    @Hint("${RUNTIME_CONFIG}/my_credentials.json")
    @Example("${RUNTIME_CONFIG}/my_credentials.json")
    @Description("The path of the Google Service Account Credentials JSON file. " +
            "Predefined runtime configuration properties (such as '${RUNTIME_CONFIG}') can be used in the path to " +
            "reference files from the ${RUNTIME_HOME}/config directory.")
    @Property("Service Account Credentials File")
    private String credentialsFile;

    @Property("Service Account User Email")
    @Hint("my.user.account@gmail.com")
    @Example("my.user.account@gmail.com")
    @Description("The email of the user account to impersonate, when the 'delegating domain-wide authority' " +
            "flag is activated for the Service Account.")
    private String credentialsEmail;

    public String getCredentialsFile() {
        return credentialsFile;
    }

    public void setCredentialsFile(String credentialsFile) {
        this.credentialsFile = credentialsFile;
    }

    public String getCredentialsEmail() {
        return credentialsEmail;
    }

    public void setCredentialsEmail(String credentialsEmail) {
        this.credentialsEmail = credentialsEmail;
    }
}
