package org.cilogon.oauth2.servlet.exceptions;

import edu.uiuc.ncsa.security.core.exceptions.GeneralException;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/11/23 at  11:00 AM
 */
public class UnknownDBSericeUserException extends GeneralException {
    public UnknownDBSericeUserException() {
    }

    public UnknownDBSericeUserException(Throwable cause) {
        super(cause);
    }

    public UnknownDBSericeUserException(String message) {
        super(message);
    }

    public UnknownDBSericeUserException(String message, Throwable cause) {
        super(message, cause);
    }
}
