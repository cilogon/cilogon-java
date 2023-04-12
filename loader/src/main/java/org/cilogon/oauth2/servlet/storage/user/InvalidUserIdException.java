package org.cilogon.oauth2.servlet.storage.user;

import edu.uiuc.ncsa.security.core.exceptions.GeneralException;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/11/13 at  11:34 AM
 */
public class InvalidUserIdException extends GeneralException {
    public InvalidUserIdException() {
    }

    public InvalidUserIdException(Throwable cause) {
        super(cause);
    }

    public InvalidUserIdException(String message) {
        super(message);
    }

    public InvalidUserIdException(String message, Throwable cause) {
        super(message, cause);
    }
}
