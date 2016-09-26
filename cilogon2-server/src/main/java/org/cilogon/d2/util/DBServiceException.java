package org.cilogon.d2.util;

/**
 * An exception that is thrown by the AbstractDBService. The message is one of the
 * given status string which is taken and sent along to the client.
 * <p>Created by Jeff Gaynor<br>
 * on Nov 19, 2010 at  3:05:03 PM
 */
public class DBServiceException extends CILogonException {
    public DBServiceException() {
    }

    public DBServiceException(Throwable cause) {
        super(cause);
    }

    public DBServiceException(String message) {
        super(message);
    }

    public DBServiceException(int message) {
        super(Integer.toString(message));
    }

    public DBServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public boolean checkMessage(String x) {
        if (getMessage() == null) {
            return x == null;
        }
        return getMessage().equals(x);
    }

    public boolean checkMessage(int statusCode) {
        this.statusCode = statusCode;
        return checkMessage(Integer.toString(statusCode));
    }

    int statusCode = -1;

    public int getStatusCode() {
        return statusCode;
    }

}
