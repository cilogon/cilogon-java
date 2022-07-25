package org.cilogon.oauth2.servlet.exceptions;

import edu.uiuc.ncsa.security.core.exceptions.GeneralException;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/25/20 at  4:56 PM
 */
public class SubjectIDMismatchException extends GeneralException {
    public SubjectIDMismatchException() {
    }

    public SubjectIDMismatchException(Throwable cause) {
        super(cause);
    }

    public SubjectIDMismatchException(String message) {
        super(message);
    }

    public SubjectIDMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
