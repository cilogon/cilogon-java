package org.cilogon.d2.servlet;

import edu.uiuc.ncsa.security.core.exceptions.TransactionNotFoundException;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import edu.uiuc.ncsa.security.servlet.ExceptionHandler;
import edu.uiuc.ncsa.security.servlet.ServletDebugUtil;
import org.cilogon.d2.storage.UserNotFoundException;
import org.cilogon.d2.util.DBServiceException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.cilogon.d2.servlet.AbstractDBService.*;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 2/9/15 at  9:55 AM
 */
public class CILogonExceptionHandler implements ExceptionHandler {
    MyLoggingFacade logger;

    @Override
    public MyLoggingFacade getLogger() {
        return logger;
    }

    AbstractDBService dbServlet;

    public CILogonExceptionHandler(AbstractDBService dbServlet, MyLoggingFacade logger) {
        this.logger = logger;
        this.dbServlet = dbServlet;
    }

    @Override
    public void handleException(Throwable t, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        ServletDebugUtil.error(this, t.getMessage(), t);
        if (t instanceof UserNotFoundException) {
            dbServlet.writeMessage(response, STATUS_USER_NOT_FOUND_ERROR);
            return;
        }
        if (t instanceof TransactionNotFoundException) {
            dbServlet.writeMessage(response, STATUS_TRANSACTION_NOT_FOUND);
            return;
        }
        if (t instanceof DBServiceException) {
            dbServlet.writeMessage(response, t.getMessage());
            return;
        }

        if(t instanceof EPTIDMismatchException){
            dbServlet.writeMessage(response, STATUS_EPTID_MISMATCH);
        }
        if(t instanceof SubjectIDMismatchException){
             dbServlet.writeMessage(response, STATUS_SUBJECT_ID_MISMATCH);
         }
        if(t instanceof PairwiseIDMismatchException){
             dbServlet.writeMessage(response, STATUS_PAIRWISE_ID_MISMATCH);
         }
        ServletDebugUtil.trace(this,"Got an error of \"" + t.getMessage() + "\", returning generic error code.");
        dbServlet.writeMessage(response, STATUS_INTERNAL_ERROR);
        // and log it too...
        dbServlet.error("There was an internal error: " + t.getMessage());
    }
}
