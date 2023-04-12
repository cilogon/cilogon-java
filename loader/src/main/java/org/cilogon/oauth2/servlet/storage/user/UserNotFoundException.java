package org.cilogon.oauth2.servlet.storage.user;


import org.cilogon.oauth2.servlet.util.CILogonException;

/**
 * <p>Created by Jeff Gaynor<br>
 * on Mar 12, 2010 at  12:52:02 PM
 */
public class UserNotFoundException extends CILogonException {
    public UserNotFoundException() {
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNotFoundException(Throwable cause) {
        super(cause);
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}
