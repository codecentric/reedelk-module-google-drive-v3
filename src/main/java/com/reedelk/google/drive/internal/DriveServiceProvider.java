package com.reedelk.google.drive.internal;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.Credentials;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.reedelk.google.drive.component.DriveConfiguration;
import com.reedelk.runtime.api.commons.StreamUtils;
import com.reedelk.runtime.api.component.Implementor;
import com.reedelk.runtime.api.exception.PlatformException;
import com.reedelk.runtime.api.resource.ResourceText;

import java.io.IOException;
import java.io.StringBufferInputStream;
import java.security.GeneralSecurityException;

import static com.reedelk.runtime.api.commons.ConfigurationPreconditions.requireNotNull;

public class DriveServiceProvider {

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    public Drive get(Class<? extends Implementor> implementor, DriveConfiguration configuration) {
        requireNotNull(implementor, configuration, "Google Drive Configuration must be provided.");

        try {
            // Build a new authorized API client service.
            ResourceText credentialsResource = configuration.getCredentials();
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Credentials credentials = getCredentials(credentialsResource);
            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);
            return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, requestInitializer)
                    .setApplicationName(implementor.getSimpleName())
                    .build();
        } catch (IOException | GeneralSecurityException e) {
            throw new PlatformException(e);
        }
    }

    private static Credentials getCredentials(ResourceText credentialsResource) throws IOException, GeneralSecurityException {
        String credentialsAsJson = StreamUtils.FromString.consume(credentialsResource.data());
        return ServiceAccountCredentials.fromStream(new StringBufferInputStream(credentialsAsJson))
                .toBuilder()
                .setScopes(DriveScopes.all())
                .build();
    }
}
