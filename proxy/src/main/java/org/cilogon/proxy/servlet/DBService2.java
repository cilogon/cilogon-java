package org.cilogon.proxy.servlet;

import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.core.exceptions.InvalidTimestampException;
import edu.uiuc.ncsa.security.core.exceptions.NFWException;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.core.util.DateUtils;
import edu.uiuc.ncsa.security.core.util.MetaDebugUtil;
import edu.uiuc.ncsa.security.core.util.StringUtils;
import edu.uiuc.ncsa.security.servlet.AbstractServlet;
import edu.uiuc.ncsa.security.servlet.ExceptionHandlerThingie;
import edu.uiuc.ncsa.security.servlet.ServletDebugUtil;
import org.cilogon.oauth2.servlet.StatusCodes;
import org.cilogon.oauth2.servlet.claims.UserClaimSource;
import org.cilogon.oauth2.servlet.impl.CILOA2AuthorizedServletUtil;
import org.cilogon.oauth2.servlet.impl.Err;
import org.cilogon.oauth2.servlet.loader.CILogonOA2ServiceEnvironment;
import org.cilogon.oauth2.servlet.servlet.AbstractDBService;
import org.cilogon.oauth2.servlet.storage.idp.IDPKeys;
import org.cilogon.oauth2.servlet.storage.transaction.CILOA2ServiceTransaction;
import org.cilogon.oauth2.servlet.storage.twofactor.TwoFactorSerializationKeys;
import org.cilogon.oauth2.servlet.storage.user.User;
import org.cilogon.oauth2.servlet.storage.user.UserKeys;
import org.cilogon.oauth2.servlet.storage.user.UserStore;
import org.oa4mp.delegation.common.token.impl.AuthorizationGrantImpl;
import org.oa4mp.delegation.common.token.impl.TokenUtils;
import org.oa4mp.delegation.server.OA2Constants;
import org.oa4mp.delegation.server.OA2Errors;
import org.oa4mp.delegation.server.OA2GeneralError;
import org.oa4mp.delegation.server.jwt.HandlerRunner;
import org.oa4mp.delegation.server.jwt.ScriptRuntimeException;
import org.oa4mp.server.api.storage.servlet.OA4MPServlet;
import org.oa4mp.server.api.util.ClientDebugUtil;
import org.oa4mp.server.loader.oauth2.OA2SE;
import org.oa4mp.server.loader.oauth2.servlet.OA2ClientUtils;
import org.oa4mp.server.loader.oauth2.servlet.RFC8628ServletConfig;
import org.oa4mp.server.loader.oauth2.servlet.RFC8628State;
import org.oa4mp.server.loader.oauth2.state.ScriptRuntimeEngineFactory;
import org.oa4mp.server.loader.oauth2.storage.RFC8628Store;
import org.oa4mp.server.loader.oauth2.storage.clients.OA2Client;
import org.oa4mp.server.loader.oauth2.storage.clients.OA2ClientApprovalKeys;
import org.oa4mp.server.loader.oauth2.storage.clients.OA2ClientKeys;
import org.oa4mp.server.loader.oauth2.storage.transactions.OA2ServiceTransaction;
import org.oa4mp.server.proxy.RFC8628Servlet;
import org.qdl_lang.exceptions.QDLException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Date;

import static org.cilogon.oauth2.servlet.StatusCodes.STATUS_CLIENT_NOT_FOUND;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/22/15 at  12:15 PM
 */
public class DBService2 extends AbstractDBService {



    public static final String CHECK_USER_CODE = "checkUserCode";
    public static final int CHECK_USER_CODE_CASE = 740;
    public static final String CHECK_CODE_APPROVED = "userCodeApproved";
    public static final int CHECK_CODE_APPROVED_CASE = 741;
    public static final String USER_CODE_PARAMETER = "user_code";
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
     //   printAllParameters(request);
        ServletDebugUtil.trace(this, "action = " + action);
        switch (lookupCase(action)) {
            case GET_CLIENT_CASE:
                getClient(request, response);
                return;
                // Near as I can tell, neither of these can be used since they do not return any
            // usable information -- just an ok
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
            case CHECK_CODE_APPROVED_CASE:
                userCodeApproved(request, response);
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
            doError("No user code parameter was found.", StatusCodes.STATUS_MISSING_ARGUMENT, response);
            return;
        }
        String userCode = request.getParameter(USER_CODE_PARAMETER);
        if (StringUtils.isTrivial(userCode)) {
            doError("No user code parameter was found.", StatusCodes.STATUS_MISSING_ARGUMENT, response);
        }
        RFC8628ServletConfig rfc8628ServletConfig = ((OA2SE) OA4MPServlet.getServiceEnvironment()).getRfc8628ServletConfig();

        userCode = RFC8628Servlet.convertToCanonicalForm(userCode, rfc8628ServletConfig);

        CILogonOA2ServiceEnvironment se = (CILogonOA2ServiceEnvironment) OA4MPServlet.getServiceEnvironment();
        if (!se.isRfc8628Enabled()) {
            doError("Device flow is not available on this server.", STATUS_SERVICE_UNAVAILABLE, response);
            return;
        }
        RFC8628Store<? extends CILOA2ServiceTransaction> rfc8628Store = (RFC8628Store<? extends CILOA2ServiceTransaction>) getTransactionStore();
        CILOA2ServiceTransaction transaction = rfc8628Store.getByUserCode(userCode);

        if (transaction == null) {
            doError("transaction not found.", STATUS_TRANSACTION_NOT_FOUND, response);
            return;
        }

        // It is possible that the transaction was garbage collected but the GC hasn't removed it
        // from the cache, so we do have to check if the ag is expired.
        AuthorizationGrantImpl ag = (AuthorizationGrantImpl) transaction.getAuthorizationGrant();
        if (ag.isExpired()) {
            doError("token not found.", STATUS_EXPIRED_TOKEN, response);
            return;
        }
//        CILOA2ServiceTransaction transaction = (CILOA2ServiceTransaction) se.getTransactionStore().get(ag);

        if (transaction == null) {
            // Then the pending transaction got garbage collected so it effectively timed out
            doError("transaction not found.", STATUS_TRANSACTION_NOT_FOUND, response);
            return;
        }
        MetaDebugUtil debugger = OA4MPServlet.createDebugger(transaction.getClient());
        debugger.trace(this, "checking transaction.");
        if (!transaction.isRFC8628Request()) {
            doError("invalid token.", STATUS_TRANSACTION_NOT_FOUND, response);
            return;
        }

        startWrite(response);
        PrintWriter printWriter = response.getWriter();
        printWriter.println(AbstractDBService.STATUS_KEY + "=" + StatusCodes.STATUS_OK);
        printWriter.println(OA2Constants.CLIENT_ID + "=" + transaction.getClient().getIdentifierString());
        debugger.trace(this, "writing response for grant = " + ag.getToken());
        printWriter.println("grant=" + TokenUtils.b32EncodeToken(ag.getToken()));
        printWriter.println("scope=" + transaction.getRFC8628State().originalScopes);
        printWriter.println("user_code=" + userCode);
        printWriter.flush();
        printWriter.close();
        stopWrite(response);
        return;


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
     */
    protected void userCodeApproved(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!request.getParameterMap().containsKey(USER_CODE_PARAMETER)) {
            // missing parameter
            doError("No user code parameter was found.", StatusCodes.STATUS_MISSING_ARGUMENT, response);
            return;
        }
        if (StringUtils.isTrivial(request.getParameter(USER_CODE_PARAMETER))) {
            doError("No user code parameter was found.", StatusCodes.STATUS_MISSING_ARGUMENT, response);
            return;
        }
        int approved = 1; // default
        /*
        This is similar to checkUserCode. If user_code parameter is given
         but empty, or if user_code parameter
        is not given, return 'missing_parameter'.

        If approved parameter is given but empty, that's the same as approved parameter is not given,
        so default to '1' (i.e., user_code is approved).
         */
        if (request.getParameterMap().containsKey(USER_CODE_APPROVED_PARAMETER)) {
            try {
                approved = Integer.parseInt(request.getParameter(USER_CODE_APPROVED_PARAMETER));
            } catch (NumberFormatException nfx) {
                doError("unknown value for " +
                                USER_CODE_APPROVED_PARAMETER + " parameter \"" +
                                request.getParameter(USER_CODE_APPROVED_PARAMETER) + "\"",
                        StatusCodes.STATUS_MISSING_ARGUMENT, response);
            }
        }
        if (approved != 0 && approved != 1) {
            doError("illegal argument approved = \"" + approved + "\"", StatusCodes.STATUS_MALFORMED_INPUT, response);
            return;
        }

        String userCode = request.getParameter(USER_CODE_PARAMETER);
        if (StringUtils.isTrivial(userCode)) {
            doError("No user code parameter was found.", StatusCodes.STATUS_MISSING_ARGUMENT, response);
            return;
        }
        RFC8628ServletConfig rfc8628ServletConfig = ((OA2SE) OA4MPServlet.getServiceEnvironment()).getRfc8628ServletConfig();
        userCode = RFC8628Servlet.convertToCanonicalForm(userCode, rfc8628ServletConfig);

        CILogonOA2ServiceEnvironment se = (CILogonOA2ServiceEnvironment) OA4MPServlet.getServiceEnvironment();
        if (!se.isRfc8628Enabled()) {
            doError("Device flow is not available on this server.", STATUS_SERVICE_UNAVAILABLE, response);
            return;
        }
        RFC8628Store<? extends CILOA2ServiceTransaction> rfc8628Store = (RFC8628Store<? extends CILOA2ServiceTransaction>) getTransactionStore();
        CILOA2ServiceTransaction transaction = rfc8628Store.getByUserCode(userCode);
        if (transaction == null) {
            doError("transaction not found.", STATUS_TRANSACTION_NOT_FOUND, response);
            return;
        }


        // It is possible that the transaction was garbage collected but the GC hasn't removed it
        // from the cache, so we do have to check if the ag is expired.
        AuthorizationGrantImpl ag = (AuthorizationGrantImpl) transaction.getAuthorizationGrant();
        MetaDebugUtil debugger = OA4MPServlet.createDebugger(transaction.getOA2Client());
        debugger.trace(this, "checking if server is RFC 8628 enabled");
        if (!transaction.isRFC8628Request()) {
            doError("invalid token.", STATUS_TRANSACTION_NOT_FOUND, response);
            return;
        }

        RFC8628State rfc8628State = transaction.getRFC8628State();
        //  RFC8628Servlet.getCache().remove(userCode);
        transaction.setUserCode(""); // zero it out so it never gets found as a pending transaction.
        if (approved == 1) {
            rfc8628State.valid = true; // means they actually logged in
            debugger.trace(this, "device flow for user code " + userCode + " approved");

        } else {
            debugger.trace(this, "device flow for user code " + userCode + " cancelled");
            // means they cancelled the whole thing. Remove the transaction and the cache entry.
            getTransactionStore().remove(transaction.getIdentifier());
            //    RFC8628Servlet.getCache().remove(userCode);
            // Now tell the system that it was cancelled. This means to return a status of 0, meaning
            // the requested action was done.
            startWrite(response);
            PrintWriter printWriter = response.getWriter();
            printWriter.println(AbstractDBService.STATUS_KEY + "=" + StatusCodes.STATUS_OK);
            printWriter.flush();
            printWriter.close();
            stopWrite(response);
            return;
        }

        transaction.setRFC8628State(rfc8628State);
        getTransactionStore().save(transaction);
        // The JSON library copies everything no matter what, so no guarantee what's in the transaction is the same object.
        // Just replace it with the good copy.
        startWrite(response);
        PrintWriter printWriter = response.getWriter();
        printWriter.println(AbstractDBService.STATUS_KEY + "=" + StatusCodes.STATUS_OK);
        printWriter.println(OA2Constants.CLIENT_ID + "=" + transaction.getClient().getIdentifierString());
        debugger.trace(this, "encoding grant for " + userCode + " = " + ag.getToken());
        printWriter.println("grant=" + TokenUtils.b32EncodeToken(ag.getToken()));
        printWriter.println("user_code=" + userCode);
        printWriter.flush();
        printWriter.close();
        stopWrite(response);
        return;

    }

    private void writeUserCodeNotFound(HttpServletResponse response) throws IOException {
        startWrite(response);
        PrintWriter printWriter = response.getWriter();
        printWriter.println(AbstractDBService.STATUS_KEY + "=0");
        printWriter.flush();
        printWriter.close();
        stopWrite(response);
    }

    protected void writeClient(OA2Client client, int statusCode, HttpServletResponse response) throws IOException {
        startWrite(response);
        ((DBServiceSerializer2) serializer).serialize(response.getWriter(), client, statusCode);
        stopWrite(response);
    }

    protected void writeTransaction(OA2ServiceTransaction oa2ServiceTransaction,
                                    int status, HttpServletResponse response) throws IOException {
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

    /**
     * This accepts the following parameters
     * <pre>
     * client_id
     * scopes
     * state
     * code_challenge          (RFC 7636)
     * code_challenge_method      "   "
     * </pre>
     * and the response
     *
     * @param req
     * @param resp
     * @throws IOException
     */
    protected void createTransaction(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ServletDebugUtil.trace(this, "createTransaction: ******** NEW CALL ******** ");
        ServletDebugUtil.trace(this, "createTransaction: printing request ");
        // Fixed https://github.com/cilogon/cilogon-java/issues/38 removed print statement
        CILOA2AuthorizedServletUtil initUtil = new CILOA2AuthorizedServletUtil(this);
        /* This next call checks that there is a client id supplied and throws an
               UnknownClientException
           if no id is found (not ideal, but we there we have it). So, in that case, this will
           be handled in the catch block below. This gives us the chance to check separately that
           all the right the parameters are sent along.
         */
        if (!req.getParameterMap().containsKey(OA2Constants.CLIENT_ID)) {
            // missing parameter
            doError("No client id parameter was found.", StatusCodes.STATUS_MISSING_ARGUMENT, resp);
            return;
        }
        String clientID = req.getParameter(OA2Constants.CLIENT_ID);
        if (clientID == null || clientID.isEmpty()) {
            //missing client id
            doError("No value for client id parameter was found.", STATUS_MISSING_CLIENT_ID, resp);
            return;
        }
        if (!req.getParameterMap().containsKey(OA2Constants.SCOPE)) {
            doError("No scopes found.", STATUS_NO_SCOPES, resp);
        } else {
            String values = req.getParameter(OA2Constants.SCOPE);
            if (-1 != values.indexOf(",")) {
                doError("No scopes found.", STATUS_MALFORMED_SCOPE, resp);
            }
        }

        Identifier client_id = null;
        try {
            client_id = BasicIdentifier.newID(clientID);
        } catch (Throwable t) {
            // invalid client id (means it did not resolve into a URI correctly
            doError("Invalid client id syntax.", StatusCodes.STATUS_MALFORMED_INPUT, resp);
            return;
        }
        if (!OA4MPServlet.getServiceEnvironment().getClientStore().containsKey(client_id)) {
            // Unknown client.
            doError("Unknown client", STATUS_UNKNOWN_CLIENT, resp);
            return;
        }
        if (!OA4MPServlet.getServiceEnvironment().getClientApprovalStore().isApproved(client_id)) {
            // unapproved client
            doError("Unapproved client.", STATUS_UNAPPROVED_CLIENT, resp);
            return;
        }
        // This also checks that the client is correct and throws an exception if not.
        OA2Client client = (OA2Client) getClient(req);
        MetaDebugUtil debugger = OA4MPServlet.createDebugger(client);

        try {
            CILOA2ServiceTransaction transaction = (CILOA2ServiceTransaction) initUtil.doDelegation(req,
                    resp,
                    true);
            if (debugger instanceof ClientDebugUtil) {
                ((ClientDebugUtil) debugger).setTransaction(transaction);
            }
            getTransactionStore().save(transaction);
            debugger.trace(this, "createTransaction: writing transaction. " + transaction);
            writeTransaction(transaction, StatusCodes.STATUS_OK, resp);
            debugger.trace(this, "createTransaction: ******** DONE ******** ");
        } catch (Throwable t) {
            if (t instanceof OA2GeneralError) {
                // Something in OA4MP proper blew up. Try to bridge the gap here with message codes.
                // CIL-1187 support. Format response with error and description.
                OA2GeneralError ge = (OA2GeneralError) t;
                debugger.trace(this, "OA2GeneralError: " + ge);
                CILOA2ExceptionHandler.YAErr yaErr = CILOA2ExceptionHandler.lookupErrorCode(ge.getError());
                if (yaErr.code == StatusCodes.STATUS_INTERNAL_ERROR) {
                    yaErr.code = STATUS_CREATE_TRANSACTION_FAILED; // what we should return for all calls to this action
                }
                debugger.trace(this, "YAErr:" + yaErr.toString());
                if (yaErr.hasMessage()) {
                    doError(yaErr.message, yaErr.code, resp);
                } else {
                    doError(ge.getDescription(), yaErr.code, resp);
                }
                return; // make sure to hop out here.
            } else {
                try {
                    getExceptionHandler().handleException(new ExceptionHandlerThingie(t, req, resp));
                } catch (Throwable xxx) {
                    // Ummm if it ends up here, it means the exception handler itself blew up and there is not a lot
                    // we can do except try to send something back.
                    getMyLogger().warn("Unrecoverable error creating transaction:\"" + t.getMessage() + "\"");
                    debugger.trace(this, "Unrecoverable error: createTransaction failed. \"" + t.getMessage() + "\".", t);
                    ServletDebugUtil.warn(this, "Unrecoverable error: Error creating transaction: \"" + t.getMessage() + "\".");
                    // CIL-570: Error codes need to be augmented so can tell why various initial errors happen.
                    // There could be a lot more of these (such documenting protocol errors and such), but this
                    // should do for most cases. If it needs to be revisted in the future, this is the place to check.
                    writeTransaction(null, StatusCodes.STATUS_INTERNAL_ERROR, resp);
                }
            }
        }
    }

    protected void writeMessage(HttpServletResponse response, Err errResponse) throws IOException {
        startWrite(response);
        ((DBServiceSerializer2) serializer).writeMessage(response.getWriter(), errResponse);
        stopWrite(response);
    }

    // Fixes CIL-101
    protected void setTransactionState(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String ag = req.getParameter(OA2Constants.AUTHORIZATION_CODE);

        if (ag == null || ag.trim().length() == 0) {
            String description = "Warning. No auth code. Cannot complete call.";
            getMyLogger().error(description);
            writeMessage(resp, new Err(StatusCodes.STATUS_MISSING_ARGUMENT, "missing_argument", description));
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
            writeTransaction(null, new Err(STATUS_EXPIRED_TOKEN, "token_expired", StatusCodes.getMessage(STATUS_EXPIRED_TOKEN)), resp);
            return;

        }
        if (!getTransactionStore().containsKey(identifier)) {
            getMyLogger().error("The auth grant \"" + authGrant + "\" is not a key for this transaction. No transaction found.");
            writeTransaction(null, new Err(STATUS_TRANSACTION_NOT_FOUND,
                    "transaction_not_found",
                    StatusCodes.getMessage(STATUS_TRANSACTION_NOT_FOUND)), resp);
            return;
        }
        Identifier userUID = BasicIdentifier.newID(req.getParameter(userKeys.identifier()));
        if (userUID == null) {
            throw new NFWException("Missing or null user id. Cannot complete call.");
        }
        UserStore userStore = ((CILogonOA2ServiceEnvironment) AbstractServlet.getEnvironment()).getUserStore();
        User user = userStore.get(userUID);
        long authTime = 0L;
        try {
            if (req.getParameter(OA2Constants.AUTHORIZATION_TIME) == null) {
                authTime = new Date().getTime();
            } else {
                authTime = Long.parseLong(req.getParameter(OA2Constants.AUTHORIZATION_TIME));
            }
        } catch (Throwable t) {
            info("Got " + OA2Constants.AUTHORIZATION_TIME + "=" + req.getParameter(OA2Constants.AUTHORIZATION_TIME) + ", error=\"" + t.getMessage() + "\"");
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
            writeTransaction(t, new Err(STATUS_TRANSACTION_NOT_FOUND, "transaction_not_found", StatusCodes.getMessage(STATUS_TRANSACTION_NOT_FOUND)), resp);
            return;
        }
        if (t == null) {
            // no transaction means there is nothing that can be done.
            getMyLogger().error("Getting the transaction for auth grant \"" + authGrant + "\" failed. No transaction found.");
            writeTransaction(t, new Err(STATUS_TRANSACTION_NOT_FOUND, "transaction_not_found", StatusCodes.getMessage(STATUS_TRANSACTION_NOT_FOUND)), resp);
            return;
        }
        MetaDebugUtil debugger = OA4MPServlet.createDebugger(t.getClient());
        if (debugger instanceof ClientDebugUtil) {
            ((ClientDebugUtil) debugger).setTransaction(t);
        }
        if (myproxyUsername == null)
        {
// https://jira.ncsa.illinois.edu/browse/CIL-2274
            t.setMyproxyUsername(user.getDN(t, true));
            debugger.info(this, "Setting myproxy username to default user DN, since no cilogon_info sent.");

        } else {
            t.setMyproxyUsername(URLDecoder.decode(myproxyUsername, "UTF-8"));
        }
        if (loa != null) {
            t.setLoa(loa);
        }


        t.setUserUID(user.getIdentifier()); // since the username can change later, set the actual uid here for all times.
        t.setAuthTime(new Date(authTime * 1000));
        user.setLastAccessed(t.getAuthTime()); // make sure user accesses are tracked as well.
        t.setAuthGrantValid(true);
        t.setUsername(userUID.toString());
       // Next block is critical since it puts the user claims from authorization in the transaction.
       // If there are server-wide claims to be processed, they are done here aw well. This takes the
       // place of the similar call in the OA4MP authorization leg, which CILogon replaces.
        try {
            debugger.trace(this, "Starting to process claims");
            doUserClaims((CILogonOA2ServiceEnvironment) OA4MPServlet.getServiceEnvironment(), t, req, debugger);
        } catch (ScriptRuntimeException srx) {
            // The user threw one of these explicitly as part of the control flow, e.g. user was not in the right group.
            debugger.trace(this, "Explicit script runtime exception:" + srx.getMessage(), srx);
            // CIL-1388
            // CIL-1342
            Err err = new Err(
                    srx.getCode() == ScriptRuntimeException.DEFAULT_NO_OP_CODE ? STATUS_QDL_ERROR : srx.getCode(),
                    srx.getRequestedType(),
                    srx.getMessage(),
                    srx.getErrorURI(),
                    srx.getCustomErrorURI());
            writeMessage(resp, err);
            return;
        } catch (Throwable throwable) {
            if (throwable instanceof QDLException) {
                QDLException qdlException = (QDLException) throwable;
                String description = qdlException.getMessage();
                debugger.trace(this, "QDL error", throwable);
                //   This is an exception from QDL, e.g. bad syntax, function called with wrong arguments, etc.
                //writeTransaction(t, new Err(STATUS_QDL_ERROR, "qdl_error", description), resp);
                writeTransaction(t, new Err(STATUS_QDL_ERROR, OA2Errors.SERVER_ERROR, description), resp);
                return;
            }
            if (throwable instanceof RuntimeException) {
                getMyLogger().error(throwable.getMessage(), throwable);
                debugger.trace(this, "Java runtime exception running QDL", throwable);
                // This is an exception thrown by some component QDL calls, e.g. a Java NPE, Java is missing a library, etc.
                //writeTransaction(t, new Err(STATUS_QDL_RUNTIME_ERROR, "qdl_encountered_an_error", throwable.getMessage()), resp);
                writeTransaction(t, new Err(STATUS_QDL_RUNTIME_ERROR, OA2Errors.SERVER_ERROR, throwable.getMessage()), resp);
                return;

            }
            getMyLogger().error("Could not get claims", throwable);
            debugger.trace(this, "Exception running QDL, throwing GeneralException", throwable);
            throw new GeneralException(throwable);
        }
        debugger.trace(this, "done setting transaction state for user " + user.getIdentifierString());
        getTransactionStore().save(t);

        writeTransaction(t, StatusCodes.STATUS_OK, resp); // just returns an ok if state written.
    }

    /**
     * Runs the user claims to populate the transaction.
     * @param env
     * @param t
     * @param request
     * @param debugger
     * @throws Throwable
     */
    protected void doUserClaims(CILogonOA2ServiceEnvironment env, CILOA2ServiceTransaction t, HttpServletRequest request, MetaDebugUtil debugger) throws Throwable {
        // try {
        debugger.trace(this, "Doing user claims");
        UserClaimSource userClaimSource = new UserClaimSource(getMyLogger());
        userClaimSource.setOa2SE((OA2SE) OA4MPServlet.getServiceEnvironment());
        t.setUserMetaData(userClaimSource.process(t.getUserMetaData(), t));
        debugger.trace(this, "Done user claims" + t.getUserMetaData().toString(1));
        debugger.trace(this, "Starting  post_auth claims");
        env.getTransactionStore().save(t); // make SURE the user claims get saved.

        HandlerRunner jwtRunner = new HandlerRunner(t, ScriptRuntimeEngineFactory.createRTE(env, t, t.getOA2Client().getConfig()));
        OA2Client resolvedClient = OA2ClientUtils.resolvePrototypes(env.getClientStore(), t.getOA2Client());
        OA2ClientUtils.setupHandlers(jwtRunner, env, t, resolvedClient, request);

        jwtRunner.doAuthClaims();
        debugger.trace(this, "Done with all post_auth claims");
    }


    // Fixes CIL-105.
    protected void getClient(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Identifier clientID = BasicIdentifier.newID(req.getParameter("client_id"));
        if(clientID == null) {
            writeMessage(resp, new Err(STATUS_MISSING_CLIENT_ID, "missing_client_id", StatusCodes.getMessage(STATUS_MISSING_CLIENT_ID)));
        }
        OA2Client client = (OA2Client) OA4MPServlet.getServiceEnvironment().getClientStore().get(clientID);
        if (client == null) {
            // None of these have been archived. We *could* check if the user has a valid uid
            // in the store and return user not found if so and user not found error if not,
            // but that would be messier to use. If this is even an issue
            writeMessage(resp, new Err(STATUS_CLIENT_NOT_FOUND, "client_not_found", StatusCodes.getMessage(STATUS_CLIENT_NOT_FOUND)));
            return;
        }
        // the last of these is the last archived user. Note that the returned list is always sorted,
        // so we just grab the last one.
        writeClient(client, StatusCodes.STATUS_OK, resp);

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
            case CHECK_CODE_APPROVED:
                return CHECK_CODE_APPROVED_CASE;
        }
        return super.lookupCase(x);
    }
}
