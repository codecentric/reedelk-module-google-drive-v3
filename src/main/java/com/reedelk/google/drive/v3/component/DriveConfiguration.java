package com.reedelk.google.drive.v3.component;

import com.reedelk.runtime.api.annotation.Description;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.annotation.Shared;
import com.reedelk.runtime.api.annotation.WidthAuto;
import com.reedelk.runtime.api.component.Implementor;
import com.reedelk.runtime.api.resource.ResourceText;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

@Shared
@Component(service = DriveConfiguration.class, scope = ServiceScope.PROTOTYPE)
public class DriveConfiguration implements Implementor {

    @Property("Application Name")
    private String applicationName;

    // TODO: This one should be from config? Or a system property?
    //  or a config property?
    @WidthAuto
    @Property("Service Account Credentials")
    private ResourceText credentials;

    @Property("Service User Email")
    @Description("The email of the user account to impersonate, if delegating domain-wide authority to the service account.")
    private String serviceAccountEmail;

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public ResourceText getCredentials() {
        return credentials;
    }

    public void setCredentials(ResourceText credentials) {
        this.credentials = credentials;
    }

    public String getServiceAccountEmail() {
        return serviceAccountEmail;
    }

    public void setServiceAccountEmail(String serviceAccountEmail) {
        this.serviceAccountEmail = serviceAccountEmail;
    }
}
