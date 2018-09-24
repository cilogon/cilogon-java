package org.cilogon.oauth2.servlet.impl;

import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.OA2SE;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.OA2ServiceTransaction;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.claims.OA2ClaimsUtil;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.storage.clients.OA2Client;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.storage.clients.OA2ClientApprovalKeys;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.storage.clients.OA2ClientKeys;
import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.exceptions.InvalidTimestampException;
import edu.uiuc.ncsa.security.core.exceptions.NFWException;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.core.util.DateUtils;
import edu.uiuc.ncsa.security.core.util.DebugUtil;
import edu.uiuc.ncsa.security.delegation.token.impl.AuthorizationGrantImpl;
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
import static edu.uiuc.ncsa.security.oauth_2_0.OA2Constants.AUTHORIZATION_CODE;
import static edu.uiuc.ncsa.security.oauth_2_0.OA2Constants.AUTHORIZATION_TIME;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/22/15 at  12:15 PM
 */
public class DBService2 extends AbstractDBService {
    public static final String GET_CLIENT = "getClient";
    public static final int GET_CLIENT_CASE = 710;
    public static final int STATUS_NO_CLIENT_FOUND = 0xFFFF;

    public static final String SET_TRANSACTION_STATE = "setTransactionState";
    public static final int SET_TRANSACTION_STATE_CASE = 720;
    public static final int STATUS_TRANSACTION_NOT_FOUND = 0x10001;
    public static final int STATUS_EXPIRED_TOKEN = 0x10003;

    public static final String CREATE_TRANSACTION_STATE = "createTransactionState";
    public static final int CREATE_TRANSACTION_STATE_CASE = 730;
    public static final int STATUS_CREATE_TRANSACTION_FAILED = 0x10005;


    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        userKeys = new UserKeys();
        idpKeys = new IDPKeys();
        tfKeys = new TwoFactorSerializationKeys();
        clientKeys = new OA2ClientKeys();
        clientApprovalKeys = new OA2ClientApprovalKeys();
        serializer = new DBServiceSerializer2(userKeys, idpKeys, tfKeys, clientKeys, clientApprovalKeys);

    }

    @Override
    protected void doAction(HttpServletRequest request, HttpServletResponse response, String action) throws IOException, ServletException {
        //printAllParameters(request);
        switch (lookupCase(action)) {
            case GET_CLIENT_CASE:
                getClient(request, response);
                return;
            case SET_TRANSACTION_STATE_CASE:
                setTransactionState(request, response);
                return;
            case CREATE_TRANSACTION_STATE_CASE:
                createTransaction(request, response);
        }

        super.doAction(request, response, action);
    }

    protected void writeClient(OA2Client client, int statusCode, HttpServletResponse response) throws IOException {
        startWrite(response);
        ((DBServiceSerializer2) serializer).serialize(response.getWriter(), client, statusCode);
        stopWrite(response);
    }

    protected void writeTransaction(OA2ServiceTransaction oa2ServiceTransaction, int statusCode, HttpServletResponse response) throws IOException {
        startWrite(response);
        ((DBServiceSerializer2) serializer).serialize(response.getWriter(), oa2ServiceTransaction, statusCode);
        stopWrite(response);
    }

    protected void createTransaction(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        DebugUtil.dbg(this, "createTransaction: ******** NEW CALL ******** ");
        CILOA2AuthorizedServletUtil initUtil = new CILOA2AuthorizedServletUtil(this);
        try {
            CILOA2ServiceTransaction transaction = (CILOA2ServiceTransaction) initUtil.doDelegation(req, resp);
            getTransactionStore().save(transaction);
            writeTransaction(transaction, STATUS_OK, resp);
            DebugUtil.dbg(this, "createTransaction: ******** DONE ******** ");
        } catch (Throwable t) {
            DebugUtil.dbg(this, "createTransaction failed. \"" + t.getMessage() + "\".");
            writeTransaction(null, STATUS_CREATE_TRANSACTION_FAILED, resp);
        }
    }

    // Fixes CIL-101
    protected void setTransactionState(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        //System.err.println(getClass().getSimpleName() + ".setTransactionState: starting");
        DebugUtil.dbg(this, "setTransactionState: ******** NEW CALL ******** ");
        String ag = req.getParameter(AUTHORIZATION_CODE);
        DebugUtil.dbg(this, "code=" + ag);

        if (ag == null || ag.trim().length() == 0) {
            getMyLogger().error("Warning. No auth code. Cannot complete call.");
            writeMessage(resp, STATUS_MISSING_ARGUMENT);
            return;
        }
        Identifier identifier = BasicIdentifier.newID(ag);
        AuthorizationGrantImpl authGrant = new AuthorizationGrantImpl(URI.create(ag));
        // Fix CIL-505
        try {
            DateUtils.checkTimestamp(ag); // if it is expired, then it will not be in the database anyway.
        } catch (InvalidTimestampException xx) {
            DebugUtil.dbg(this, "expired token " + ag);
            getMyLogger().error("The auth grant \"" + ag + "\" is expired. No transaction found.");
            writeTransaction(null, STATUS_EXPIRED_TOKEN, resp);
            return;

        }
        DebugUtil.dbg(this, "AuthGrant= " + authGrant.toString());
        if (!getTransactionStore().containsKey(identifier)) {
            DebugUtil.dbg(this, "Failed to get transaction for key " + identifier);
            getMyLogger().error("The auth grant \"" + authGrant + "\" is not a key for this transaction. No transaction found.");
            writeTransaction(null, STATUS_TRANSACTION_NOT_FOUND, resp);
            return;
        }
        Identifier userUID = newID(req.getParameter(userKeys.identifier()));
        if (userUID == null) {
            throw new NFWException("Missing or null user id. Cannot complete call.");
        }
        UserStore userStore = ((CILogonOA2ServiceEnvironment) getEnvironment()).getUserStore();
        User user = userStore.get(userUID);
        DebugUtil.dbg(this, "user=" + user.toString());
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
        DebugUtil.dbg(this, "LOA=" + loa);
        String myproxyUsername = req.getParameter("cilogon_info");
        debug(getClass().getSimpleName() + ".setTransState: cilogon_info=" + myproxyUsername);
        CILOA2ServiceTransaction t = null;
        // Make sure that if there is some internal issue getting a transaction that a random runtime exception
        // is unhandled. In particular, if a user waits a very long time before trying to get an access token,
        // their transaction may have expired and been garbage collected. Fail gracefully.
        try {
            DebugUtil.dbg(this, "Attempting to get transaction for key=" + authGrant);
            t = (CILOA2ServiceTransaction) getTransaction(authGrant);
            DebugUtil.dbg(this, "   Success");
        } catch (Throwable throwable) {
            DebugUtil.dbg(this, "Failed to get transaction for key=" + authGrant + ". Reason=" + throwable.getMessage());

            getMyLogger().error("Getting the transaction for auth grant \"" + authGrant + "\" failed.", throwable);
            writeTransaction(t, STATUS_TRANSACTION_NOT_FOUND, resp);
            return;
        }
        if (t == null) {
            // no transaction means there is nothing that can be done.
            DebugUtil.dbg(this, "Got null transaction for key=" + authGrant);

            getMyLogger().error("Getting the transaction for auth grant \"" + authGrant + "\" failed. No transaction found.");
            writeTransaction(t, STATUS_TRANSACTION_NOT_FOUND, resp);
            return;
        }
        if (myproxyUsername == null) {
            t.setMyproxyUsername(user.getDN(t, true));
            info("Setting myproxy username to default user DN, since no cilogon_info sent.");
        } else {
            debug("setting myproxy username");
            t.setMyproxyUsername(URLDecoder.decode(myproxyUsername, "UTF-8"));
        }
        if (loa != null) {
            DebugUtil.dbg(this, "setTransactionState: setting LOA to " + loa);
            t.setLoa(loa);
        }


        t.setAuthTime(new Date(authTime * 1000));
        t.setAuthGrantValid(true);
        t.setUsername(userUID.toString());

        doClaims((CILogonOA2ServiceEnvironment) getServiceEnvironment(), t);


        getTransactionStore().save(t);

        DebugUtil.dbg(this, "setTransactionState:transaction saved " + getTransactionStore().get(t.getAuthorizationGrant()));

        writeTransaction(t, STATUS_OK, resp);
    }

    protected void doClaims(CILogonOA2ServiceEnvironment env, CILOA2ServiceTransaction t) {
         /*
        This is the first place we can get claims for the user. We requrie the user, some existing claims
        (which we append to) and the transaction.
        This is a side effect of this call.
         */
        try {
            OA2ClaimsUtil claimsUtil = new OA2ClaimsUtil(env, t);
            // This gets us the basic claims.
            UserClaimSource userClaimSource = new UserClaimSource(getMyLogger());
            userClaimSource.setOa2SE((OA2SE) getServiceEnvironment());
            JSONObject claims = claimsUtil.createBasicClaims(null, t);

            userClaimSource.process(claims, t);
            t.setClaims(claims);
            DebugUtil.dbg(this, "stored claims =" + claims);
        } catch (Throwable throwable) {
            DebugUtil.dbg(this, "Claims processing failed. Reason=" + throwable.getMessage());
            getMyLogger().error("Claims processing failed.", throwable);
            return;
        }
    }

    // Fixes CIL-105.
    protected void getClient(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Identifier clientID = newID(req.getParameter("client_id"));
        OA2Client client = (OA2Client) getServiceEnvironment().getClientStore().get(clientID);
        debug("getting client = " + client);
        if (client == null) {
            // None of these have been archived. We *could* check if the user has a valid uid
            // in the store and return user not found if so and user not found error if not,
            // but that would be messier to use. If this is even an issue
            writeMessage(resp, STATUS_NO_CLIENT_FOUND);
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
        return super.lookupCase(x);
    }
}
