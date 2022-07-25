package org.cilogon.oauth2.servlet.exceptions;

import edu.uiuc.ncsa.security.core.exceptions.GeneralException;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/25/20 at  4:56 PM
 */
public class PairwiseIDMismatchException extends GeneralException {
    public PairwiseIDMismatchException() {
    }

    public PairwiseIDMismatchException(Throwable cause) {
        super(cause);
    }

    public PairwiseIDMismatchException(String message) {
        super(message);
    }

    public PairwiseIDMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
