package com.reedelk.google.drive.v3.internal.commons;

import com.reedelk.google.drive.v3.component.PermissionType;
import com.reedelk.runtime.api.exception.ComponentConfigurationException;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.junit.jupiter.api.Test;

import static com.reedelk.google.drive.v3.internal.commons.PermissionUtils.checkPreconditions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PermissionUtilsTest {

    @Test
    void shouldNotThrowExceptionWhenTypeUserAndEmailNotBlank() {
        // Expect
        assertDoesNotThrow(() ->
                checkPreconditions(
                        PermissionType.USER,
                        DynamicString.from("my@email.com"),
                        DynamicString.from("")));
    }

    @Test
    void shouldNotThrowExceptionWhenTypeGroupAndEmailNotBlank() {
        // Expect
        assertDoesNotThrow(() ->
                checkPreconditions(
                        PermissionType.GROUP,
                        DynamicString.from("my@email.com"),
                        DynamicString.from("")));
    }

    @Test
    void shouldNotThrowExceptionWhenTypeDomainAndDomainNotBlank() {
        // Expect
        assertDoesNotThrow(() ->
                checkPreconditions(
                        PermissionType.DOMAIN,
                        DynamicString.from(""),
                        DynamicString.from("MyDomain")));
    }

    @Test
    void shouldThrowExceptionWhenTypeUserAndEmailEmpty() {
        // Expect
        ComponentConfigurationException thrown = assertThrows(ComponentConfigurationException.class, () ->
                checkPreconditions(
                        PermissionType.USER,
                        DynamicString.from(""),
                        DynamicString.from("MyDomain")));

        // Then
        assertThat(thrown)
                .hasMessage("PermissionCreate (com.reedelk.google.drive.v3.component.PermissionCreate) " +
                        "has a configuration error: Email Address must not be empty when permission type is user or group.");
    }

    @Test
    void shouldThrowExceptionWhenTypeDomainAndDomainEmpty() {
        // Expect
        ComponentConfigurationException thrown = assertThrows(ComponentConfigurationException.class, () ->
                checkPreconditions(
                        PermissionType.DOMAIN,
                        DynamicString.from("my@email.com"),
                        DynamicString.from("")));

        // Then
        assertThat(thrown)
                .hasMessage("PermissionCreate (com.reedelk.google.drive.v3.component.PermissionCreate) " +
                        "has a configuration error: Domain must not be empty when permission type is domain.");
    }
}