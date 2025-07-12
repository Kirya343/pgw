package org.workswap.main.exceptions;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

public class UserNotRegisteredException extends OAuth2AuthenticationException {
    private final String email;

    public UserNotRegisteredException(String email) {
        super(new OAuth2Error(
                "user_not_registered",
                "User with email " + email + " is not registered",
                null
        ));
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}