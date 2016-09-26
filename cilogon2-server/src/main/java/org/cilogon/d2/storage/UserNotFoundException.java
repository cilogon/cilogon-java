package org.cilogon.d2.storage;


import org.cilogon.d2.util.CILogonException;

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
