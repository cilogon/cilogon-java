package org.cilogon.oauth2.servlet.exceptions;

import edu.uiuc.ncsa.security.core.exceptions.GeneralException;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/24/17 at  1:34 PM
 */
public class EPTIDMismatchException extends GeneralException {
    public EPTIDMismatchException() {
    }

    public EPTIDMismatchException(Throwable cause) {
        super(cause);
    }

    public EPTIDMismatchException(String message) {
        super(message);
    }

    public EPTIDMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
