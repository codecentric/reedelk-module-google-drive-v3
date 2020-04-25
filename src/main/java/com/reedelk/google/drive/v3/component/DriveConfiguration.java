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

    @WidthAuto
    @Property("Service Account Credentials")
    private String credentials;

    @Property("Service User Email")
    @Description("The email of the user account to impersonate, if delegating domain-wide authority to the service account.")
    private String serviceAccountEmail;

    public String getCredentials() {
        return credentials;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    public String getServiceAccountEmail() {
        return serviceAccountEmail;
    }

    public void setServiceAccountEmail(String serviceAccountEmail) {
        this.serviceAccountEmail = serviceAccountEmail;
    }
}
