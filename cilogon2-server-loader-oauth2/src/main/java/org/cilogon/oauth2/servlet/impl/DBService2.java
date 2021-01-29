package org.cilogon.oauth2.servlet.impl;

import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.OA2SE;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.OA2ServiceTransaction;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.claims.OA2ClaimsUtil;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.servlet.OA2ClientUtils;
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
import edu.uiuc.ncsa.security.delegation.token.impl.AuthorizationGrantImpl;
import edu.uiuc.ncsa.security.oauth_2_0.OA2Constants;
import edu.uiuc.ncsa.security.oauth_2_0.jwt.JWTRunner;
import edu.uiuc.ncsa.security.servlet.ServletDebugUtil;
import net.sf.json.JSONObject;
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
        }
        super.doAction(request, response, action);
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
        ServletDebugUtil.trace(this, "createTransaction failed: \"" + message + "\".");
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
        if(!req.getParameterMap().containsKey(SCOPE)){
            doError("No scopes found.", STATUS_NO_SCOPES, resp);
        }else{
            String values = req.getParameter(SCOPE);
            if(-1 != values.indexOf(",")){
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
            CILOA2ServiceTransaction transaction = (CILOA2ServiceTransaction) initUtil.doDelegation(req, resp);
            getTransactionStore().save(transaction);
            ServletDebugUtil.trace(this, "createTransaction: writing transaction. " + transaction);
            writeTransaction(transaction, STATUS_OK, resp);
            ServletDebugUtil.trace(this, "createTransaction: ******** DONE ******** ");
        } catch (Throwable t) {
            // grab bag of errors from lower down in the stack.
            getMyLogger().warn("Error creating transaction:\"" + t.getMessage() + "\"");
            ServletDebugUtil.trace(this, "createTransaction failed. \"" + t.getMessage() + "\".", t);
            ServletDebugUtil.warn(this, "Error creating transaction: \"" + t.getMessage() + "\".");
            // CIL-570: Error codes need to be augmented so can tell why various initial errors happen.
            // There could be a lot more of these (such documenting protocol errors and such), but this
            // should do for most cases. If it needs to be revisted in the future, this is the place to check.
            writeTransaction(null, 42, resp);
        }
    }

    public static class Err{
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
        ((DBServiceSerializer2)serializer).writeMessage(response.getWriter(), errResponse);
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
            String description =   "Getting the transaction for auth grant \"" + authGrant + "\" failed.";
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
        }catch (Throwable throwable){
            if(throwable instanceof RuntimeException ){
                throw (RuntimeException)throwable;
            }
            getMyLogger().error("Could not get claims", throwable);
            throw new GeneralException(throwable);
        }
        getTransactionStore().save(t);

        writeTransaction(t, STATUS_OK, resp);
    }

    protected void doClaims2(CILogonOA2ServiceEnvironment env, CILOA2ServiceTransaction t, HttpServletRequest request) throws Throwable {
        try {
            DebugUtil.trace(this,"Doing user claims");
            UserClaimSource userClaimSource = new UserClaimSource(getMyLogger());
            userClaimSource.setOa2SE((OA2SE) getServiceEnvironment());
            t.setUserMetaData(userClaimSource.process(t.getUserMetaData(), t));
            DebugUtil.trace(this,"Done user claims" + t.getUserMetaData().toString(1));
            DebugUtil.trace(this,"Starting  post_auth claims");
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

    protected void doClaims(CILogonOA2ServiceEnvironment env, CILOA2ServiceTransaction t) {


         /*
        This is the first place we can get claims for the user. We require the user, some existing claims
        (which we append to) and the transaction.
        This is a side effect of this call.
         */
        try {
            OA2ClaimsUtil claimsUtil = new OA2ClaimsUtil(env, t);
            // This gets us the basic claims.
            UserClaimSource userClaimSource = new UserClaimSource(getMyLogger());
            userClaimSource.setOa2SE((OA2SE) getServiceEnvironment());
            JSONObject claims = claimsUtil.processAuthorizationClaims(null);
            userClaimSource.process(claims, t);
            t.setUserMetaData(claims);
        } catch (Throwable throwable) {
            getMyLogger().error("Claims processing failed.", throwable);
            return;
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
        if (x.equals(GET_CLIENT)) return GET_CLIENT_CASE;
        if (x.equals(SET_TRANSACTION_STATE)) return SET_TRANSACTION_STATE_CASE;
        if (x.equals(CREATE_TRANSACTION_STATE)) return CREATE_TRANSACTION_STATE_CASE;  // CIL-467 needs this
        return super.lookupCase(x);
    }
}
