package org.cilogon.oauth2.servlet.impl;

import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.OA2SE;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.OA2ServiceTransaction;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.servlet.OA2ClientUtils;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.servlet.RFC8628Servlet;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.servlet.RFC8628State;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.state.ScriptRuntimeEngineFactory;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.storage.clients.OA2Client;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.storage.clients.OA2ClientApprovalKeys;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.storage.clients.OA2ClientKeys;
import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.core.exceptions.InvalidTimestampException;
import edu.uiuc.ncsa.security.core.exceptions.NFWException;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.core.util.DateUtils;
import edu.uiuc.ncsa.security.core.util.DebugUtil;
import edu.uiuc.ncsa.security.core.util.StringUtils;
import edu.uiuc.ncsa.security.delegation.token.impl.AuthorizationGrantImpl;
import edu.uiuc.ncsa.security.delegation.token.impl.TokenUtils;
import edu.uiuc.ncsa.security.oauth_2_0.OA2Constants;
import edu.uiuc.ncsa.security.oauth_2_0.OA2GeneralError;
import edu.uiuc.ncsa.security.oauth_2_0.jwt.JWTRunner;
import edu.uiuc.ncsa.security.servlet.ServletDebugUtil;
import org.cilogon.d2.servlet.AbstractDBService;
import org.cilogon.d2.storage.User;
import org.cilogon.d2.storage.UserStore;
import org.cilogon.d2.twofactor.TwoFactorSerializationKeys;
import org.cilogon.d2.util.IDPKeys;
import org.cilogon.d2.util.UserKeys;
import org.cilogon.oauth2.servlet.claims.UserClaimSource;
import org.cilogon.oauth2.servlet.loader.CILogonOA2ServiceEnvironment;
import org.cilogon.oauth2.servlet.storage.CILOA2ServiceTransaction;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Date;

import static edu.uiuc.ncsa.security.core.util.BasicIdentifier.newID;
import static edu.uiuc.ncsa.security.oauth_2_0.OA2Constants.*;
import static org.cilogon.d2.servlet.StatusCodes.*;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/22/15 at  12:15 PM
 */
public class DBService2 extends AbstractDBService {
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
    public static final int STATUS_SERVICE_UNAVAILABLE = 0x10015; //65557

    public static final String CHECK_USER_CODE = "checkUserCode";
    public static final int CHECK_USER_CODE_CASE = 740;
    public static final String USER_CODE_PARAMETER = "userCode";
    public static final String USER_CODE_APPROVED_PARAMETER = "approved";
    //public static final int STATUS_MISSING = 0x10013; //65555


    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        userKeys = new UserKeys();
        idpKeys = new IDPKeys();
        tfKeys = new TwoFactorSerializationKeys();
        clientKeys = new OA2ClientKeys();
        clientApprovalKeys = new OA2ClientApprovalKeys();
        serializer = new DBServiceSerializer2(userKeys, idpKeys, tfKeys, clientKeys, clientApprovalKeys);
        setExceptionHandler(new CILOA2ExceptionHandler(this, getMyLogger()));
    }

    @Override
    protected void doAction(HttpServletRequest request, HttpServletResponse response, String action) throws IOException, ServletException {
        printAllParameters(request);
        ServletDebugUtil.trace(this, "action = " + action);
        switch (lookupCase(action)) {
            case GET_CLIENT_CASE:
                getClient(request, response);
                return;
            case SET_TRANSACTION_STATE_CASE:
                setTransactionState(request, response);
                return;
            case CREATE_TRANSACTION_STATE_CASE:
                ServletDebugUtil.trace(this, "creating transaction");
                createTransaction(request, response);
                return;
            case CHECK_USER_CODE_CASE:
                checkUserCode(request, response);
                return;
        }
        super.doAction(request, response, action);
    }

    /**
     * Taken from <a href="https://jira.ncsa.illinois.edu/browse/CIL-934">CIL-934</a>
     * action: checkUserCode
     * param: user_code (required, but can be empty)
     * <p>
     * Purpose:
     * This is an "internal" dbService method used by the PHP web front end to
     * (1) verify that a user_code input by the user is valid and
     * (2) return the client_id associated with this transaction in order to display client
     * information to the end user. The user_code parameter is required, but it can be empty.
     * The user_code parameter can contain extra "user-friendly" characters such as
     * dash '-', space ' ', underscore '_', etc. These extra characters will be stripped
     * out/ignored by the dbService. The user_code can contain lower-case and/or
     * upper-case characters which will be transformed to upper-case characters by the dbService.
     * Returns: HTTP 200 response, body is basic text, one line per returned value:
     * <p>
     * status=INTEGER
     * 0 = Success
     * 1048569 = missing parameter
     * 65537 = transaction not found
     * 65539 = expired user_code (token)
     * client_id=The OIDC client_id matching the user_code
     * user_code=The original user_code to be displayed to the end user. The purpose of this
     * is that the returned user_code should visually match the one that was returned to
     * the device so the user can easily verify a match (i.e., ignore any
     * transformations done by the user when inputting the user_code).
     * scope=A (possibly empty/absent) space-separated list of scopes that were requested by
     * the client. This is needed when displaying the list of attributes to be delegated
     * since the scopes requested by the device client may differ from those registered.
     *
     * @param request
     * @param response
     * @throws IOException
     */
    protected void checkUserCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!request.getParameterMap().containsKey(USER_CODE_PARAMETER)) {
            // missing parameter
            doError("No user code parameter was found.", STATUS_MISSING_ARGUMENT, response);
            return;
        }
        String userCode = request.getParameter(USER_CODE_PARAMETER);
        if (StringUtils.isTrivial(userCode)) {
            doError("No user code parameter was found.", STATUS_MISSING_ARGUMENT, response);
        }
        userCode = RFC8628Servlet.convertToCanonicalForm(userCode);

        CILogonOA2ServiceEnvironment se = (CILogonOA2ServiceEnvironment) getServiceEnvironment();
        if (!se.isRfc8628Enabled()) {
            doError("Device flow is not available on this server.", STATUS_SERVICE_UNAVAILABLE, response);
            return;
        }
        if (RFC8628Servlet.getCache().containsKey(userCode)) {
            String rawAG = RFC8628Servlet.getCache().get(userCode);
            if (StringUtils.isTrivial(rawAG)) {
                doError("token not found.", STATUS_EXPIRED_TOKEN, response);
            }

            // It is possible that the transaction was garbage collected but the GC hasn't removed it
            // from the cache, so we do have to check if the ag is expired.
            AuthorizationGrantImpl ag = new AuthorizationGrantImpl(URI.create(rawAG));
            if (ag.isExpired()) {
                doError("token not found.", STATUS_EXPIRED_TOKEN, response);
                return;
            }
            CILOA2ServiceTransaction transaction = (CILOA2ServiceTransaction) se.getTransactionStore().get(ag);

            if (transaction == null) {
                // Then the pending transaction got garbage collected so it effectively timed out
                doError("transaction not found.", STATUS_TRANSACTION_NOT_FOUND, response);
                return;
            }
            if (!transaction.isRFC8628Request()) {
                doError("invalid token.", STATUS_TRANSACTION_NOT_FOUND, response);
                return;
            }
            String scopes = "";
            if (!transaction.getScopes().isEmpty()) {
                boolean firstPass = true;
                for (String s : transaction.getScopes()) {
                    if (firstPass) {
                        firstPass = false;
                        scopes = s;
                    } else {
                        scopes = scopes + " " + s;
                    }
                }
            }

            startWrite(response);
            PrintWriter printWriter = response.getWriter();
            printWriter.println(STATUS_KEY + "=" + STATUS_OK);
            printWriter.println(CLIENT_ID + "=" + transaction.getClient().getIdentifierString());
            printWriter.println("grant=" + TokenUtils.b32EncodeToken(ag.getToken()));
            printWriter.println("scope=" + scopes);
            printWriter.println("user_code=" + userCode);
            printWriter.flush();
            printWriter.close();
            stopWrite(response);
            return;
        }

        writeUserCodeNotFound(response);

    }

    /**
     * action: userCodeApproved
     * param(s):
     * <p>
     * user_code (required)
     * approved (optional; defaults to 1; 1=approved; 0=denied)
     * <p>
     * Purpose: This is an "internal" dbService method used by the PHP web front end
     * to let the dbService know that the user has logged on to their
     * chosen Identity Provider and approved the transaction OR
     * that the user has clicked a "Cancel" button and denied the transaction.
     * If the user has approved the transaction (approved=1 or 'approved'
     * is absent, the default), the OA4MP server can proceed with the
     * rest of the Device authz grant flow. If the user has denied the
     * transaction (approved=0), the OA4MP server should inform the device
     * that the user has canceled the transaction.
     * Returns: HTTP 200 response, body is basic text, one line per returned value:
     * <p>
     * status=INTEGER
     * 0 = Success
     * 1048569 = missing parameter
     * 65537 = transaction not found
     * 65539 = expired user_code (token)
     */
    protected void userCodeApproved(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!request.getParameterMap().containsKey(USER_CODE_PARAMETER)) {
            // missing parameter
            doError("No user code parameter was found.", STATUS_MISSING_ARGUMENT, response);
            return;
        }
        int approved = 1; // default
        if (request.getParameterMap().containsKey(USER_CODE_APPROVED_PARAMETER)) {
            try {
                approved = Integer.parseInt(request.getParameter(USER_CODE_APPROVED_PARAMETER));
            } catch (NumberFormatException nfx) {
                doError("unknown value for " +
                                USER_CODE_APPROVED_PARAMETER + " parameter \"" +
                                request.getParameter(USER_CODE_APPROVED_PARAMETER) + "\"",
                        STATUS_MISSING_ARGUMENT, response);
            }
        }
        String userCode = request.getParameter(USER_CODE_PARAMETER);
        if (StringUtils.isTrivial(userCode)) {
            doError("No user code parameter was found.", STATUS_MISSING_ARGUMENT, response);
        }
        userCode = RFC8628Servlet.convertToCanonicalForm(userCode);

        CILogonOA2ServiceEnvironment se = (CILogonOA2ServiceEnvironment) getServiceEnvironment();
        if (!se.isRfc8628Enabled()) {
            doError("Device flow is not available on this server.", STATUS_SERVICE_UNAVAILABLE, response);
            return;
        }
        if (RFC8628Servlet.getCache().containsKey(userCode)) {
            String rawAG = RFC8628Servlet.getCache().get(userCode);
            if (StringUtils.isTrivial(rawAG)) {
                doError("token not found.", STATUS_EXPIRED_TOKEN, response);
            }

            // It is possible that the transaction was garbage collected but the GC hasn't removed it
            // from the cache, so we do have to check if the ag is expired.
            AuthorizationGrantImpl ag = new AuthorizationGrantImpl(URI.create(rawAG));
            if (ag.isExpired()) {
                doError("token not found.", STATUS_EXPIRED_TOKEN, response);
                writeUserCodeNotFound(response);
                return;
            }
            CILOA2ServiceTransaction transaction = (CILOA2ServiceTransaction) se.getTransactionStore().get(ag);
            if (transaction == null) {
                // Then the pending transaction got garbage collected so it effectively timed out
                doError("transaction not found.", STATUS_TRANSACTION_NOT_FOUND, response);
                return;
            }
            if (!transaction.isRFC8628Request()) {
                doError("invalid token.", STATUS_TRANSACTION_NOT_FOUND, response);
                return;
            }

            RFC8628State rfc8628State = transaction.getRFC8628State();
            RFC8628Servlet.getCache().remove(userCode);

            if(approved == 1){
                rfc8628State.valid = true; // means they actually logged in

            } else{
                // means they cancelled the whole thing. Remove the transaction
                getTransactionStore().remove(transaction.getIdentifier());
            }
            // The JSON library copies everything no matter what, so no guarantee what's in the transaction is the same object.
            // Just replace it with the good copy.
            startWrite(response);
            PrintWriter printWriter = response.getWriter();
            printWriter.println(STATUS_KEY + "=" + STATUS_OK);
            printWriter.println(CLIENT_ID + "=" + transaction.getClient().getIdentifierString());
            printWriter.println("grant=" + TokenUtils.b32EncodeToken(ag.getToken()));
            printWriter.println("user_code=" + userCode);
            printWriter.flush();
            printWriter.close();
            stopWrite(response);
            return;
        }

    }

    private void writeUserCodeNotFound(HttpServletResponse response) throws IOException {
        startWrite(response);
        PrintWriter printWriter = response.getWriter();
        printWriter.println(STATUS_KEY + "=0");
        printWriter.flush();
        printWriter.close();
        stopWrite(response);
    }

    protected void writeClient(OA2Client client, int statusCode, HttpServletResponse response) throws IOException {
        startWrite(response);
        ((DBServiceSerializer2) serializer).serialize(response.getWriter(), client, statusCode);
        stopWrite(response);
    }

    protected void writeTransaction(OA2ServiceTransaction oa2ServiceTransaction, int status, HttpServletResponse response) throws IOException {
        startWrite(response);
        ((DBServiceSerializer2) serializer).serialize(response.getWriter(), oa2ServiceTransaction, status);
        stopWrite(response);
    }

    protected void writeTransaction(OA2ServiceTransaction oa2ServiceTransaction, Err errResponse, HttpServletResponse response) throws IOException {
        startWrite(response);
        ((DBServiceSerializer2) serializer).serialize(response.getWriter(), oa2ServiceTransaction, errResponse);
        stopWrite(response);
    }

    protected void doError(String message, int errorCode, HttpServletResponse resp) throws IOException {
        ServletDebugUtil.trace(this, "createTransaction failed: \"" + message + "\", code=" + errorCode);
        writeTransaction(null, new Err(errorCode, "create_transaction_failed", message), resp);
    }

    protected void createTransaction(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ServletDebugUtil.trace(this, "createTransaction: ******** NEW CALL ******** ");
        ServletDebugUtil.trace(this, "createTransaction: printing request ");
        ServletDebugUtil.printAllParameters(this.getClass(), req);
        CILOA2AuthorizedServletUtil initUtil = new CILOA2AuthorizedServletUtil(this);
        /* This next call checks that there is a client id supplied and throws an
               UnknownClientException
           if no id is found (not ideal, but we there we have it). So, in that case, this will
           be handled in the catch block below. This gives us the chance to check separately that
           all the right the parameters are sent along.
         */
        if (!req.getParameterMap().containsKey(OA2Constants.CLIENT_ID)) {
            // missing parameter
            doError("No client id parameter was found.", STATUS_MISSING_ARGUMENT, resp);
            return;
        }
        String clientID = req.getParameter(OA2Constants.CLIENT_ID);
        if (clientID == null || clientID.isEmpty()) {
            //missing client id
            doError("No value for client id parameter was found.", STATUS_MISSING_CLIENT_ID, resp);
            return;
        }
        if (!req.getParameterMap().containsKey(SCOPE)) {
            doError("No scopes found.", STATUS_NO_SCOPES, resp);
        } else {
            String values = req.getParameter(SCOPE);
            if (-1 != values.indexOf(",")) {
                doError("No scopes found.", STATUS_MALFORMED_SCOPE, resp);
            }

        }

        Identifier client_id = null;
        try {
            client_id = BasicIdentifier.newID(clientID);
        } catch (Throwable t) {
            // invalid client id (means it did not resolve into a URI correctly
            doError("Invalid client id syntax.", STATUS_MALFORMED_INPUT, resp);
            return;
        }
        if (!getServiceEnvironment().getClientStore().containsKey(client_id)) {
            // Unknown client.
            doError("Unknown client", STATUS_UNKNOWN_CLIENT, resp);
            return;
        }
        if (!getServiceEnvironment().getClientApprovalStore().isApproved(client_id)) {
            // unapproved client
            doError("Unapproved client.", STATUS_UNAPPROVED_CLIENT, resp);
            return;
        }
        // Kept his next line, even though we don't explicitly do anything with the client.
        // This checks that the client is correct and throws an exception if not.
        getClient(req);


        try {
            CILOA2ServiceTransaction transaction = (CILOA2ServiceTransaction) initUtil.doDelegation(req, resp, true);
            getTransactionStore().save(transaction);
            ServletDebugUtil.trace(this, "createTransaction: writing transaction. " + transaction);
            writeTransaction(transaction, STATUS_OK, resp);
            ServletDebugUtil.trace(this, "createTransaction: ******** DONE ******** ");
        } catch (Throwable t) {
            if (t instanceof OA2GeneralError) {
                // Something in OA4MP proper blew up. Try to bridge the gap here with message codes.

                OA2GeneralError ge = (OA2GeneralError) t;
                ServletDebugUtil.trace(this, "OA2GeneralError: " + ge.toString());
                CILOA2ExceptionHandler.YAErr yaErr = CILOA2ExceptionHandler.lookupErrorCode(ge.getError());
                if (yaErr.code == STATUS_INTERNAL_ERROR) {
                    yaErr.code = STATUS_CREATE_TRANSACTION_FAILED; // what we should return for all calls to this action
                }
                ServletDebugUtil.trace(this, "YAErr:" + yaErr.toString());
                if (yaErr.hasMessage()) {
                    doError(yaErr.message, yaErr.code, resp);
                } else {
                    doError(ge.getDescription(), yaErr.code, resp);
                }
                return; // make sure to hop out here.
            } else {
                try {
                    getExceptionHandler().handleException(t, req, resp);
                } catch (Throwable xxx) {
                    // Ummm if it ends up here, it means the exception handler itself blew up and there is not a lot
                    // we can do except try to send something back.
                    getMyLogger().warn("Unrecoverable error creating transaction:\"" + t.getMessage() + "\"");
                    ServletDebugUtil.trace(this, "Unrecoverable error: createTransaction failed. \"" + t.getMessage() + "\".", t);
                    ServletDebugUtil.warn(this, "Unrecoverable error: Error creating transaction: \"" + t.getMessage() + "\".");
                    // CIL-570: Error codes need to be augmented so can tell why various initial errors happen.
                    // There could be a lot more of these (such documenting protocol errors and such), but this
                    // should do for most cases. If it needs to be revisted in the future, this is the place to check.
                    writeTransaction(null, STATUS_INTERNAL_ERROR, resp);
                }
            }
        }
    }

    public static class Err {
        public Err(int code, String error, String description) {
            this.code = code;
            this.error = error;
            this.description = description;
        }

        int code;
        String description;
        String error;
    }

    protected void writeMessage(HttpServletResponse response, Err errResponse) throws IOException {
        startWrite(response);
        ((DBServiceSerializer2) serializer).writeMessage(response.getWriter(), errResponse);
        stopWrite(response);
    }

    // Fixes CIL-101
    protected void setTransactionState(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String ag = req.getParameter(AUTHORIZATION_CODE);

        if (ag == null || ag.trim().length() == 0) {
            String description = "Warning. No auth code. Cannot complete call.";
            getMyLogger().error(description);
            writeMessage(resp, new Err(STATUS_MISSING_ARGUMENT, "missing_argument", description));
            return;
        }
        if (TokenUtils.isBase32(ag)) {
            ag = TokenUtils.b32DecodeToken(ag);
        }
        Identifier identifier = BasicIdentifier.newID(ag);
        AuthorizationGrantImpl authGrant = new AuthorizationGrantImpl(URI.create(ag));
        // Fix CIL-505
        try {
            DateUtils.checkTimestamp(ag); // if it is expired, then it will not be in the database anyway.
        } catch (InvalidTimestampException xx) {
            String description = "The auth grant \"" + ag + "\" is expired. No transaction found.";
            getMyLogger().error(description);
            writeTransaction(null, new Err(STATUS_EXPIRED_TOKEN, "token_expired", getMessage(STATUS_EXPIRED_TOKEN)), resp);
            return;

        }
        if (!getTransactionStore().containsKey(identifier)) {
            getMyLogger().error("The auth grant \"" + authGrant + "\" is not a key for this transaction. No transaction found.");
            writeTransaction(null, new Err(STATUS_TRANSACTION_NOT_FOUND,
                    "transaction_not_found",
                    getMessage(STATUS_TRANSACTION_NOT_FOUND)), resp);
            return;
        }
        Identifier userUID = newID(req.getParameter(userKeys.identifier()));
        if (userUID == null) {
            throw new NFWException("Missing or null user id. Cannot complete call.");
        }
        UserStore userStore = ((CILogonOA2ServiceEnvironment) getEnvironment()).getUserStore();
        User user = userStore.get(userUID);
        long authTime = 0L;
        try {
            if (req.getParameter(AUTHORIZATION_TIME) == null) {
                authTime = new Date().getTime();
            } else {
                authTime = Long.parseLong(req.getParameter(AUTHORIZATION_TIME));
            }
        } catch (Throwable t) {
            info("Got " + AUTHORIZATION_TIME + "=" + req.getParameter(AUTHORIZATION_TIME) + ", error=\"" + t.getMessage() + "\"");
        }
        String loa = req.getParameter("loa");
        String myproxyUsername = req.getParameter("cilogon_info");
        CILOA2ServiceTransaction t = null;
        // Make sure that if there is some internal issue getting a transaction that a random runtime exception
        // is unhandled. In particular, if a user waits a very long time before trying to get an access token,
        // their transaction may have expired and been garbage collected. Fail gracefully.
        try {
            t = (CILOA2ServiceTransaction) getTransaction(authGrant);
        } catch (Throwable throwable) {
            String description = "Getting the transaction for auth grant \"" + authGrant + "\" failed.";
            getMyLogger().error(description, throwable);
            writeTransaction(t, new Err(STATUS_TRANSACTION_NOT_FOUND, "transaction_not_found", getMessage(STATUS_TRANSACTION_NOT_FOUND)), resp);
            return;
        }
        if (t == null) {
            // no transaction means there is nothing that can be done.
            getMyLogger().error("Getting the transaction for auth grant \"" + authGrant + "\" failed. No transaction found.");
            writeTransaction(t, new Err(STATUS_TRANSACTION_NOT_FOUND, "transaction_not_found", getMessage(STATUS_TRANSACTION_NOT_FOUND)), resp);
            return;
        }
        if (myproxyUsername == null) {
            t.setMyproxyUsername(user.getDN(t, true));
            info("Setting myproxy username to default user DN, since no cilogon_info sent.");
        } else {
            t.setMyproxyUsername(URLDecoder.decode(myproxyUsername, "UTF-8"));
        }
        if (loa != null) {
            t.setLoa(loa);
        }


        t.setAuthTime(new Date(authTime * 1000));
        t.setAuthGrantValid(true);
        t.setUsername(userUID.toString());

        //doClaims((CILogonOA2ServiceEnvironment) getServiceEnvironment(), t);
        try {
            doClaims2((CILogonOA2ServiceEnvironment) getServiceEnvironment(), t, req);
        } catch (Throwable throwable) {
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException) throwable;
            }
            getMyLogger().error("Could not get claims", throwable);
            throw new GeneralException(throwable);
        }
        getTransactionStore().save(t);

        writeTransaction(t, STATUS_OK, resp);
    }

    protected void doClaims2(CILogonOA2ServiceEnvironment env, CILOA2ServiceTransaction t, HttpServletRequest request) throws Throwable {
        try {
            DebugUtil.trace(this, "Doing user claims");
            UserClaimSource userClaimSource = new UserClaimSource(getMyLogger());
            userClaimSource.setOa2SE((OA2SE) getServiceEnvironment());
            t.setUserMetaData(userClaimSource.process(t.getUserMetaData(), t));
            DebugUtil.trace(this, "Done user claims" + t.getUserMetaData().toString(1));
            DebugUtil.trace(this, "Starting  post_auth claims");
            env.getTransactionStore().save(t); // make SURE the user claims get saved.

            JWTRunner jwtRunner = new JWTRunner(t, ScriptRuntimeEngineFactory.createRTE(env, t, t.getOA2Client().getConfig()));
            OA2ClientUtils.setupHandlers(jwtRunner, env, t, request);

            jwtRunner.doAuthClaims();
        } catch (Throwable throwable) {
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException) throwable;
            }
            getMyLogger().error("error processing claims: " + throwable.getMessage(), throwable);
            if (DebugUtil.getDebugLevel() == DebugUtil.DEBUG_LEVEL_TRACE) {
                throwable.printStackTrace();
            }
            throw new GeneralException("Error processing claims", throwable);
        }

    }


    // Fixes CIL-105.
    protected void getClient(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Identifier clientID = newID(req.getParameter("client_id"));
        OA2Client client = (OA2Client) getServiceEnvironment().getClientStore().get(clientID);
        if (client == null) {
            // None of these have been archived. We *could* check if the user has a valid uid
            // in the store and return user not found if so and user not found error if not,
            // but that would be messier to use. If this is even an issue
            writeMessage(resp, new Err(STATUS_NO_CLIENT_FOUND, "client_not_found", getMessage(STATUS_CLIENT_NOT_FOUND)));
            return;
        }
        // the last of these is the last archived user. Note that the returned list is always sorted,
        // so we just grab the last one.
        writeClient(client, STATUS_OK, resp);

    }

    @Override
    protected int lookupCase(String x) {
        switch (x) {
            case GET_CLIENT:
                return GET_CLIENT_CASE;
            case SET_TRANSACTION_STATE:
                return SET_TRANSACTION_STATE_CASE;
            case CREATE_TRANSACTION_STATE:
                return CREATE_TRANSACTION_STATE_CASE;
            case CHECK_USER_CODE:
                return CHECK_USER_CODE_CASE;
        }
        return super.lookupCase(x);
    }
}
