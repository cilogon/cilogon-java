package org.cilogon.oauth2.servlet.impl;

import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import edu.uiuc.ncsa.security.oauth_2_0.OA2GeneralError;
import org.cilogon.d2.servlet.AbstractDBService;
import org.cilogon.d2.servlet.CILogonExceptionHandler;
import org.cilogon.d2.servlet.StatusCodes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/29/21 at  10:56 AM
 */
public class CILOA2ExceptionHandler extends CILogonExceptionHandler {
    public CILOA2ExceptionHandler(AbstractDBService dbServlet, MyLoggingFacade logger) {
        super(dbServlet, logger);
    }

    @Override
    public void handleException(Throwable t, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if(t instanceof OA2GeneralError){
            OA2GeneralError ge = (OA2GeneralError)t;
            DBService2.Err err = new DBService2.Err(StatusCodes.STATUS_INTERNAL_ERROR, ge.getError(), ge.getDescription()) ;
            ((DBService2)dbServlet).writeMessage(response, err);
            return;
        }
        super.handleException(t, request, response);
    }
}
