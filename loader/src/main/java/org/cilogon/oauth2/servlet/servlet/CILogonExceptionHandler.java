package org.cilogon.oauth2.servlet.servlet;

import edu.uiuc.ncsa.security.core.exceptions.TransactionNotFoundException;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import edu.uiuc.ncsa.security.servlet.ExceptionHandler;
import edu.uiuc.ncsa.security.servlet.ServletDebugUtil;
import org.cilogon.oauth2.servlet.StatusCodes;
import org.cilogon.oauth2.servlet.exceptions.EPTIDMismatchException;
import org.cilogon.oauth2.servlet.exceptions.PairwiseIDMismatchException;
import org.cilogon.oauth2.servlet.exceptions.SubjectIDMismatchException;
import org.cilogon.oauth2.servlet.storage.user.UserNotFoundException;
import org.cilogon.oauth2.servlet.util.DBServiceException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 2/9/15 at  9:55 AM
 */
public abstract class CILogonExceptionHandler implements ExceptionHandler {
    protected MyLoggingFacade logger;

    @Override
    public MyLoggingFacade getLogger() {
        return logger;
    }

   protected AbstractDBService dbServlet;

    public CILogonExceptionHandler(AbstractDBService dbServlet, MyLoggingFacade logger) {
        this.logger = logger;
        this.dbServlet = dbServlet;
    }

    @Override
    public void handleException(Throwable t, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        ServletDebugUtil.error(this, t.getMessage(), t);

        if (t instanceof UserNotFoundException) {
            dbServlet.writeMessage(response, StatusCodes.STATUS_USER_NOT_FOUND_ERROR);
            return;
        }
        if (t instanceof TransactionNotFoundException) {
            dbServlet.writeMessage(response, StatusCodes.STATUS_TRANSACTION_NOT_FOUND);
            return;
        }
        if (t instanceof DBServiceException) {
            dbServlet.writeMessage(response, t.getMessage());
            return;
        }

        if(t instanceof EPTIDMismatchException){
            dbServlet.writeMessage(response, StatusCodes.STATUS_EPTID_MISMATCH);
        }
        if(t instanceof SubjectIDMismatchException){
             dbServlet.writeMessage(response, StatusCodes.STATUS_SUBJECT_ID_MISMATCH);
         }
        if(t instanceof PairwiseIDMismatchException){
             dbServlet.writeMessage(response, StatusCodes.STATUS_PAIRWISE_ID_MISMATCH);
         }
        ServletDebugUtil.trace(this,"Got an error of \"" + t.getMessage() + "\", returning generic error code.");
        dbServlet.writeMessage(response, StatusCodes.STATUS_INTERNAL_ERROR);
        // and log it too...
        dbServlet.error("There was an internal error: " + t.getMessage());
    }
}
