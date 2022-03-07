package org.cilogon.proxy.servlet;

import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import edu.uiuc.ncsa.security.oauth_2_0.OA2Errors;
import edu.uiuc.ncsa.security.oauth_2_0.OA2GeneralError;
import org.cilogon.d2.servlet.AbstractDBService;
import org.cilogon.d2.servlet.CILogonExceptionHandler;
import org.cilogon.d2.servlet.StatusCodes;
import org.cilogon.oauth2.servlet.impl.Err;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.cilogon.proxy.servlet.DBService2.*;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/29/21 at  10:56 AM
 */
public class CILOA2ExceptionHandler extends CILogonExceptionHandler implements OA2Errors {

    public CILOA2ExceptionHandler(AbstractDBService dbServlet, MyLoggingFacade logger) {
        super(dbServlet, logger);
    }

    @Override
    public void handleException(Throwable t, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (t instanceof OA2GeneralError) {
            OA2GeneralError ge = (OA2GeneralError) t;
            Err err = new Err(StatusCodes.STATUS_INTERNAL_ERROR, ge.getError(), ge.getDescription());
            ((DBService2) dbServlet).writeMessage(response, err);
            return;
        }
        super.handleException(t, request, response);

    }

    /**
     * Yet Another {@link Err} type object. This links
     * {@link edu.uiuc.ncsa.security.oauth_2_0.OA2Errors} to {@link StatusCodes}.
     * Note that not setting the message to null means that whatever description OA4MP
     * generated will be used. These are generally very informative for system programmers
     * but not necessarily so for end users. This allows customization.
     */
    public static class YAErr {
        public YAErr(int code, String message) {
            this.code = code;
            this.message = message;
        }

        int code;
        String message;
        public boolean hasMessage(){return message!= null;}

        @Override
        public String toString() {
            return "YAErr{" +
                    "code=" + code +
                    ", message='" + message + '\'' +
                    '}';
        }
    }

        /*
          public static final String GET_CLIENT = "getClient";
    public static final int GET_CLIENT_CASE = 710;
    public static final int STATUS_NO_CLIENT_FOUND = 0xFFFF; //65535

    public static final String SET_TRANSACTION_STATE = "setTransactionState";
    public static final int SET_TRANSACTION_STATE_CASE = 720;
    public static final int STATUS_TRANSACTION_NOT_FOUND = 0x10001; //65537
    public static final int STATUS_EXPIRED_TOKEN = 0x10003; //65539

    public static final String CREATE_TRANSACTION_STATE = "createTransaction";
    public static final int CREATE_TRANSACTION_STATE_CASE = 730;
    public static final int STATUS_CREATE_TRANSACTION_FAILED = 0x10005; // 65541
    public static final int STATUS_UNKNOWN_CALLBACK = 0x10007; // 65543
    public static final int STATUS_MISSING_CLIENT_ID = 0x10009; //65545
    public static final int STATUS_NO_REGISTERED_CALLBACKS = 0x1000B; // 65547
    public static final int STATUS_UNKNOWN_CLIENT = 0x1000D; // 65549
    public static final int STATUS_UNAPPROVED_CLIENT = 0x1000F; // 65551
    public static final int STATUS_NO_SCOPES = 0x10011; //65553
    public static final int STATUS_MALFORMED_SCOPE = 0x10013; //65555
         */

    /**
     * A practical note is that an awful lot of the errors that OA4MP generates are edge cases
     * (such as a non-existent response_type) or very marginal at best. Generally the OA4MP
     * errors are pretty succinct about what happened. E.g. {@link OA2Errors#INVALID_GRANT}
     * may refer to any of
     * <ul>
     *     <li>an expired authorization grant</li>
     *     <li>an authorization grant that has been invalidated (probably be cause it was used already</li>
     *     <li>an authorization grant that is legitimately past expiration</li>
     *     <li>a bogus authorization grant that is unreocognized by the system.</li>
     * </ul>
     * This will be disambiguated in the description of the {@link OA2GeneralError}
     * that is thrown. This method will let you override any or all of these messages
     * as you see fit.
     * @param oa2Error
     * @return
     */
    public static YAErr lookupErrorCode(String oa2Error) {
        switch (oa2Error) {
            case OA2Errors.INTERACTION_REQUIRED:
                return new YAErr(StatusCodes.STATUS_INTERNAL_ERROR, null);
            case OA2Errors.LOGIN_REQUIRED:
                return new YAErr(StatusCodes.STATUS_INTERNAL_ERROR, null);
            case OA2Errors.ACCOUNT_SELECTION_REQUIRED:
                return new YAErr(StatusCodes.STATUS_INTERNAL_ERROR, null);
            case OA2Errors.CONSENT_REQUIRED:
                return new YAErr(StatusCodes.STATUS_INTERNAL_ERROR, null);
            case OA2Errors.INVALID_REQUEST_OBJECT:
                return new YAErr(StatusCodes.STATUS_INTERNAL_ERROR, null);
            case OA2Errors.INVALID_REQUEST:
                return new YAErr(StatusCodes.STATUS_INTERNAL_ERROR, null);
            case OA2Errors.INVALID_REQUEST_URI:
                return new YAErr(StatusCodes.STATUS_INTERNAL_ERROR, null);
            case OA2Errors.UNAUTHORIZED_CLIENT:
                return new YAErr(STATUS_NO_CLIENT_FOUND, null);
            case OA2Errors.ACCESS_DENIED:
                return new YAErr(StatusCodes.STATUS_INTERNAL_ERROR, null);
            case OA2Errors.UNSUPPORTED_RESPONSE_TYPE:
                return new YAErr(StatusCodes.STATUS_INTERNAL_ERROR, null);
            case OA2Errors.INVALID_SCOPE:
                return new YAErr(STATUS_MALFORMED_SCOPE, null);
            case OA2Errors.TEMPORARILY_UNAVAILABLE:
                return new YAErr(StatusCodes.STATUS_INTERNAL_ERROR, null);
            case OA2Errors.SERVER_ERROR:
                return new YAErr(StatusCodes.STATUS_INTERNAL_ERROR, null);
            case OA2Errors.INVALID_TOKEN:
                return new YAErr(STATUS_EXPIRED_TOKEN, null);
            case OA2Errors.INVALID_GRANT:
                return new YAErr(STATUS_EXPIRED_TOKEN, null);
            case OA2Errors.INVALID_TARGET:
                return new YAErr(StatusCodes.STATUS_INTERNAL_ERROR, null);
            default:
                // If some error is generated this system does not recognize, it will return this.
                return new YAErr(StatusCodes.STATUS_INTERNAL_ERROR, "general error");
        }
    }
}
