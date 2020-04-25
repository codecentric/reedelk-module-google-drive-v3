package com.reedelk.google.drive.v3.internal;

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
import com.reedelk.google.drive.v3.component.DriveConfiguration;
import com.reedelk.runtime.api.commons.StringUtils;
import com.reedelk.runtime.api.component.Implementor;
import com.reedelk.runtime.api.exception.ComponentConfigurationException;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

import static com.reedelk.google.drive.v3.internal.commons.Messages.Misc.DRIVE_CREDENTIALS_ERROR;
import static com.reedelk.google.drive.v3.internal.commons.Messages.Misc.DRIVE_CREDENTIALS_ERROR_GENERIC;
import static com.reedelk.runtime.api.commons.ComponentPrecondition.Configuration.requireNotBlank;
import static com.reedelk.runtime.api.commons.ComponentPrecondition.Configuration.requireNotNull;

public class DriveApiFactory {

    private DriveApiFactory() {
    }

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    public static DriveApi create(Class<? extends Implementor> implementor, DriveConfiguration configuration) {
        requireNotNull(implementor, configuration, "Google Drive Configuration must be provided.");
        requireNotBlank(implementor, configuration.getCredentialsFile(), "Service Account Credentials File is missing.");

        // Build a new authorized API client service.
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            final Credentials credentials = createCredentialsFrom(implementor, configuration);
            final HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);
            final Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, requestInitializer)
                    .setApplicationName(implementor.getSimpleName())
                    .build();
            return new DriveApi(service);
        } catch (GeneralSecurityException | IOException exception) {
            String error = DRIVE_CREDENTIALS_ERROR_GENERIC.format(exception.getMessage());
            throw new ComponentConfigurationException(implementor, error);
        }
    }

    private static Credentials createCredentialsFrom(Class<? extends Implementor> implementor, DriveConfiguration configuration) {
        String credentialsFilePath = configuration.getCredentialsFile();
        String serviceAccountEmail = configuration.getCredentialsEmail();
        try (FileInputStream is = new FileInputStream(credentialsFilePath)) {
            ServiceAccountCredentials.Builder builder = ServiceAccountCredentials.fromStream(is)
                    .toBuilder()
                    .setScopes(DriveScopes.all());
            if (StringUtils.isNotBlank(serviceAccountEmail)) {
                // The email of the user account to impersonate. The email of the user account
                // is mandatory when we want to perform domain-wide delegation.
                builder.setServiceAccountUser(serviceAccountEmail);
            }
            return builder.build();
        } catch (IOException exception) {
            String error = DRIVE_CREDENTIALS_ERROR.format(credentialsFilePath, exception.getMessage());
            throw new ComponentConfigurationException(implementor, error);
        }
    }
}
