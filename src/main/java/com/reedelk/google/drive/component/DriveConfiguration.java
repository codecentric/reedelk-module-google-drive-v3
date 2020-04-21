package com.reedelk.google.drive.component;

import com.reedelk.core.component.ResourceReadText;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.annotation.Shared;
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
    @Property("Service Account Credentials")
    private ResourceText credentials;

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
}
