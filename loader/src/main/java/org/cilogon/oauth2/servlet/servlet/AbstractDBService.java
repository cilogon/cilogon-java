package org.cilogon.oauth2.servlet.servlet;


import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.core.exceptions.NFWException;
import edu.uiuc.ncsa.security.core.exceptions.TransactionNotFoundException;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.core.util.StringUtils;
import edu.uiuc.ncsa.security.servlet.ServletDebugUtil;
import edu.uiuc.ncsa.security.storage.sql.ConnectionRecord;
import org.cilogon.oauth2.servlet.StatusCodes;
import org.cilogon.oauth2.servlet.exceptions.EPTIDMismatchException;
import org.cilogon.oauth2.servlet.exceptions.PairwiseIDMismatchException;
import org.cilogon.oauth2.servlet.exceptions.SubjectIDMismatchException;
import org.cilogon.oauth2.servlet.exceptions.UnknownDBSericeUserException;
import org.cilogon.oauth2.servlet.storage.archiveUser.ArchivedUser;
import org.cilogon.oauth2.servlet.storage.archiveUser.ArchivedUserStore;
import org.cilogon.oauth2.servlet.storage.idp.IDPKeys;
import org.cilogon.oauth2.servlet.storage.idp.IdentityProvider;
import org.cilogon.oauth2.servlet.storage.idp.IdentityProviderStore;
import org.cilogon.oauth2.servlet.storage.transaction.CILSQLTransactionStore;
import org.cilogon.oauth2.servlet.storage.twofactor.TwoFactorInfo;
import org.cilogon.oauth2.servlet.storage.twofactor.TwoFactorSerializationKeys;
import org.cilogon.oauth2.servlet.storage.twofactor.TwoFactorStore;
import org.cilogon.oauth2.servlet.storage.user.*;
import org.cilogon.oauth2.servlet.util.CILogonSE;
import org.cilogon.oauth2.servlet.util.DBServiceException;
import org.cilogon.oauth2.servlet.util.DBServiceSerializer;
import org.oa4mp.delegation.common.storage.clients.Client;
import org.oa4mp.delegation.common.storage.clients.ClientApprovalKeys;
import org.oa4mp.delegation.common.storage.clients.ClientKeys;
import org.oa4mp.delegation.common.token.AuthorizationGrant;
import org.oa4mp.delegation.common.token.TokenForge;
import org.oa4mp.delegation.server.ServiceTransaction;
import org.oa4mp.delegation.server.request.IssuerResponse;
import org.oa4mp.server.api.ServiceConstantKeys;
import org.oa4mp.server.api.ServiceEnvironmentImpl;
import org.oa4mp.server.api.storage.servlet.OA4MPServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.BitSet;
import java.util.Collection;
import java.util.LinkedList;

import static edu.uiuc.ncsa.security.core.util.BasicIdentifier.newID;
import static edu.uiuc.ncsa.security.core.util.StringUtils.isTrivial;
import static org.apache.http.HttpStatus.SC_OK;
import static org.cilogon.oauth2.servlet.StatusCodes.*;
import static org.oa4mp.server.api.ServiceConstantKeys.FORM_ENCODING_KEY;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/19/12 at  4:23 PM
 */
public abstract class AbstractDBService extends OA4MPServlet {


    public static final String ACTION_PARAMETER = "action";
    public static final String CREATE_USER = "createUser";
    public static final String GET_ALL_IDPS = "getAllIdps";
    public static final String GET_LAST_ARCHIVED_USER = "getLastArchivedUser";
    public static final String GET_PORTAL_PARAMETER = "getPortalParameter";
    public static final String GET_TWO_FACTOR_INFO = "getTwoFactorInfo";
    public static final String GET_USER = "getUser";
    public static final String GET_USER_ID = "getUserID";
    public static final String HAS_USER = "hasUser";
    public static final String REMOVE_USER = "removeUser";
    public static final String SET_ALL_IDPS = "setAllIdps";
    public static final String SET_TWO_FACTOR_INFO = "setTwoFactorInfo";
    public static final String UPDATE_USER = "updateUser";
    public static final int CREATE_USER_CASE = 120;
    public static final int GET_ALL_IDPS_CASE = 400;
    public static final int GET_LAST_ARCHIVED_USER_CASE = 300;
    public static final int GET_PORTAL_PARAMETER_CASE = 200;
    public static final int GET_TWO_FACTOR_INFO_CASE = 800;
    public static final int GET_USER_CASE = 100;
    public static final int GET_USER_ID_CASE = 110;
    public static final int HAS_USER_CASE = 550;
    public static final int REMOVE_USER_CASE = 600;
    public static final int SET_ALL_IDPS_CASE = 500;
    public static final int SET_TWO_FACTOR_INFO_CASE = 810;
    public static final int UNRECOGNIZED_CASE = -1;
    public static final int UPDATE_USER_CASE = 650;

    public static final String STATUS_KEY = "status";

    public static final String AFFILIATION = "affiliation";
    public static final String ATTR_JSON = "attr_json";
    public static final String DISPLAY_NAME = "display_name";
    public static final String OU = "ou";
    public static final String REGISTERED_BY_INCOMMON = " registered_by_incommon";

    public static final String GET_CLIENT = "getClient";
    public static final int GET_CLIENT_CASE = 710;
    public static final int STATUS_NO_CLIENT_FOUND = 0xFFFF; //65535

    public static final String SET_TRANSACTION_STATE = "setTransactionState";
    public static final int SET_TRANSACTION_STATE_CASE = 720;

    public static final String CREATE_TRANSACTION_STATE = "createTransaction";
    public static final int CREATE_TRANSACTION_STATE_CASE = 730;
    public static final int STATUS_TRANSACTION_NOT_FOUND = 0x10001; //65537
    public static final int STATUS_EXPIRED_TOKEN = 0x10003; //65539
    public static final int STATUS_CREATE_TRANSACTION_FAILED = 0x10005; // 65541
    public static final int STATUS_MISSING_CLIENT_ID = 0x10009; //65545
    public static final int STATUS_UNKNOWN_CLIENT = 0x1000D; // 65549
    public static final int STATUS_UNAPPROVED_CLIENT = 0x1000F; // 65551
    public static final int STATUS_NO_SCOPES = 0x10011; //65553
    public static final int STATUS_MALFORMED_SCOPE = 0x10013; //65555
    public static final int STATUS_SERVICE_UNAVAILABLE = 0x10015; //65557
    public static final int STATUS_QDL_ERROR = 0x100007; // 1048583
    public static final int STATUS_QDL_RUNTIME_ERROR = 0x100009; // 1048585
    /*
    The following codes are inherited from the original Perl code. Even values indicate a success,
     odd values are for errors.
     */


    /**
     * There is no DN column in the database since  it is computed based on the current
     * state of the user. However, we do need to transfer this calling applications.
     */
    public static final String distinguishedNameField = "distinguished_name";
    protected org.cilogon.oauth2.servlet.storage.user.UserKeys userKeys;
    protected IDPKeys idpKeys;
    protected TwoFactorSerializationKeys tfKeys;
    protected ClientKeys clientKeys;
    protected ClientApprovalKeys clientApprovalKeys;


    protected DBServiceSerializer serializer;

    @Override
    public void init() throws ServletException {
        super.init();
        setExceptionHandler(new CILogonExceptionHandler(this, getMyLogger()) {
        });
    }

    @Override
    protected void doIt(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        ServletDebugUtil.trace(this, "doIt: request = \"" + request.getRequestURL().toString()
                + "\", query = \"" + request.getQueryString() + "\"");
        if (getCILSE().getDBServiceConfig().isEnabled()) {
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            if (isTrivial(username) || isTrivial(password)) {
                throw new UnknownDBSericeUserException();
            }
            getCILSE().getDBServiceConfig().checkPassword(username, password);
        }
        String action = getParam(request, ACTION_PARAMETER);
        if (doPing(request, response)) {
            return;
        }
        doAction(request, response, action);
    }

    /**
     * Override this to add more actions.
     *
     * @param request
     * @param response
     * @param action
     * @throws IOException
     * @throws ServletException
     */
    protected void doAction(HttpServletRequest request, HttpServletResponse response, String action) throws IOException, ServletException {
        //printAllParameters(request);
        switch (lookupCase(action)) {
            case GET_USER_ID_CASE:
                getUserID(request, response);
                break;
            case GET_USER_CASE:
                getUser(request, response);
                break;
            case CREATE_USER_CASE:
                createUser(request, response);
                break;
            case HAS_USER_CASE:
                hasUser(request, response);
                break;
            case GET_PORTAL_PARAMETER_CASE:
                getPortalParameter(request, response);
                break;
            case SET_ALL_IDPS_CASE:
                setAllIdps(request, response);
                break;
            case GET_ALL_IDPS_CASE:
                getAllIdps(request, response);
                break;
            case REMOVE_USER_CASE:
                removeUser(request, response);
                break;
            case UPDATE_USER_CASE:
                updateUser(request, response);
                break;
            case GET_LAST_ARCHIVED_USER_CASE:
                getLastArchivedUser(request, response);
                break;
            case SET_TWO_FACTOR_INFO_CASE:
                set2FInfo(request, response);
                break;
            case GET_TWO_FACTOR_INFO_CASE:
                get2FInfo(request, response);
                break;
            default:
                info("Action \"" + action + "\" not found");
                throw new DBServiceException(STATUS_ACTION_NOT_FOUND);
        }
    }

    ServiceEnvironmentImpl getSE() throws IOException {
        return (ServiceEnvironmentImpl) OA4MPServlet.getServiceEnvironment();
    }

    CILogonSE getCILSE() throws IOException {
        return (CILogonSE) OA4MPServlet.getServiceEnvironment();
    }

    UserStore getUserStore() throws IOException {
        return getCILSE().getUserStore();
    }

    ArchivedUserStore getArchivedUserStore() throws IOException {
        return getCILSE().getArchivedUserStore();
    }

    TwoFactorStore get2FStore() throws IOException {
        return getCILSE().getTwoFactorStore();
    }

    IdentityProviderStore getIDPStore() throws IOException {
        return getCILSE().getIDPStore();
    }

    TokenForge getTokenForge() throws IOException {
        return getSE().getTokenForge();
    }

    private void set2FInfo(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String useruidString = getParam(request, tfKeys.identifier(), true);
        if (useruidString == null) {
            writeMessage(response, STATUS_MISSING_ARGUMENT);
            return;
        }
        Identifier uid = newID(useruidString);
        String info = getParam(request, tfKeys.info(), true);
        try {
            getUserStore().get(uid);
        } catch (UserNotFoundException ux) {
            writeMessage(response, STATUS_USER_NOT_FOUND_ERROR); // this is not an error, just info.
            return;
        }
        TwoFactorInfo tfi = new TwoFactorInfo(uid, info);
        get2FStore().save(tfi);
        writeMessage(response, STATUS_OK);


    }

    private void get2FInfo(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String useruidString = getParam(request, tfKeys.identifier(), true);
        if (useruidString == null) {
            writeMessage(response, STATUS_USER_NOT_FOUND_ERROR); // this is not an error, just info.
        }
        TwoFactorInfo tfi = get2FStore().get(newID(useruidString));
        if (tfi == null || tfi.getInfo() == null || tfi.getInfo().length() == 0) {
            setStatusOK(response); // this is not an error, just info.
        }
        serializer.serialize(response.getWriter(), tfi, STATUS_OK);

    }

    private void removeUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String useruidString = getParam(request, userKeys.identifier(), true);
        User user = null;
        if (useruidString != null) {
            user = getUserStore().get(newID(useruidString));
            getArchivedUserStore().archiveUser(user.getIdentifier());
            getUserStore().remove(user.getIdentifier());
            setStatusOK(response);
        } else {
            writeMessage(response, STATUS_USER_NOT_FOUND_ERROR); // this is not an error, just info.
        }
    }

    // have to do this a lot, so put it all here.
    UserMultiID getNames(HttpServletRequest request) throws UnsupportedEncodingException {
        String x = getParam(request, userKeys.remoteUser(), true);

        RemoteUserName remoteUser = null;
        EduPersonTargetedID eptid = null;
        PairwiseID pairwiseID = null;
        SubjectID subjectID = null;
        EduPersonPrincipleName eppn = null;
        OpenID openid = null;
        OpenIDConnect oidc = null;
        if (!isEmpty(x)) remoteUser = new RemoteUserName(x);

        x = getParam(request, userKeys.eppn(), true);
        if (!isEmpty(x)) eppn = new EduPersonPrincipleName(x);

        x = getParam(request, userKeys.eptid(), true);
        if (!isEmpty(x)) eptid = new EduPersonTargetedID(x);

        x = getParam(request, userKeys.pairwiseId(), true);
        if (!isEmpty(x)) {
            pairwiseID = new PairwiseID(x);
        }

        x = getParam(request, userKeys.subjectId(), true);
        if (!isEmpty(x)) {
            subjectID = new SubjectID(x);
        }

        x = getParam(request, userKeys.openID(), true);
        if (!isEmpty(x)) openid = new OpenID(x);

        x = getParam(request, userKeys.oidc(), true);
        if (!isEmpty(x)) oidc = new OpenIDConnect(x);

        if (remoteUser != null && eppn != null && eptid != null && openid != null && oidc != null && pairwiseID != null && subjectID != null) {
            throw new IllegalStateException("Error: Cannot have all ids specified.");
        }
        UserMultiID names = new UserMultiID(remoteUser, eppn, eptid, openid, oidc, pairwiseID, subjectID);
        return names;
    }


    protected void getUserbyUID(HttpServletRequest request, HttpServletResponse response, String useruidString) throws IOException {
        Identifier uid = newID(useruidString);
        try {
            User user = getUserStore().get(uid);
            ServletDebugUtil.trace(this, "Got user by id. uid=" + user.getIdentifierString() + ", serial string = " + user.getSerialString());

            TwoFactorInfo tfi = get2FStore().get(uid);
            writeUser(user, tfi, STATUS_OK, response);
            return;
        } catch (UserNotFoundException x) {
            writeMessage(response, STATUS_USER_NOT_FOUND_ERROR);
            return;
        }
    }


    protected void getUser(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Fix https://github.com/cilogon/cilogon-java/issues/38 -- re-enable for local debugging only if needed
        //    printAllParameters(request);

        // case 1: a user id is supplied. Return information about the user.
        // Easy case -- the request has the unique identifier.
        ServletDebugUtil.trace(this, "starting get user");
        String useruidString = getParam(request, userKeys.identifier(), true);
        ServletDebugUtil.trace(this, "useruidString = " + useruidString);
        if (useruidString != null) {
            getUserbyUID(request, response, useruidString);
            // Note the last call writes the response, so nothing further needs to be done.
            return;
        }
        // check if there is a multi-ID
        UserMultiID userMultiKey = getNames(request);
        ServletDebugUtil.trace(this, "no user uid: Multi-id isTrivial? " + userMultiKey.isTrivial() + " = " + userMultiKey);
        if (userMultiKey.isTrivial()) {
            ServletDebugUtil.trace(this, "trivial multi-id =" + userMultiKey);
            throw new DBServiceException(STATUS_MISSING_ARGUMENT);
        }

        String idp = getParam(request, userKeys.idp(), true);
        if (StringUtils.isTrivial(idp)) {
            writeMessage(response, STATUS_MISSING_ARGUMENT);
        }
        // Strategy to not break existing CILogon. If the user exists with the given IDP,
        // return that. If not, return most recently created as per CIL-2201.
        // The motivation for the ticket is that universities can change their IDPs without warning,
        // resulting in users that cannot be located this way.
        User user = null;
        try {
            findUser(userMultiKey, idp); // completely standard, get the user and return it.
            getUserWithIDP(request, response, userMultiKey, idp);
            //writeUser(user, StatusCodes.STATUS_OK, response);
            return;
        } catch (UserNotFoundException userNotFoundException) {
            // no such user for that IDP.
        }
        user = getUserNoIDP(request, response, userMultiKey);
        if (user == null) {
            // new user. treat as per legacy case
            getUserWithIDP(request, response, userMultiKey, idp);
            return;
        }
        // new case, we found at least one user with the given (unique) identifier
        // and must now update the IDP and determine if we will update the serial
        // string too.
        UserProperties userProperties = getUserProperties(request);
        boolean keepSS = updateUserfromProperties(user, userProperties);
        // The user may send several identifiers, e.g. EPPN, EPTID and subject id
        // and some of these may change. If we are here, at least one of them did not
        // but that is the best we can say.
        user.setUserMultiKey(userMultiKey);
        user.setIdP(idp); // since the IDP changed, reset it.
        getUserStore().updateCheckSerialString(user, keepSS);
        writeUser(user, STATUS_IDP_UPDATED, response);
    }

    boolean equalWithNull(String x, String y) {
        if (x == null) {
            if (y == null) {
                return true;
            } else {
                return false;
            }
        } else {
            if (y == null) {
                return false;
            } else {
                return x.equals(y);
            }
        }
    }

    /**
     * User properties are sent in the request. This does the laborious task of updating them
     * and returns true if the change to require a serial string update.
     *
     * @param user
     * @param userProperties
     * @return
     */
    protected boolean updateUserfromProperties(User user, UserProperties userProperties) {
        boolean keepSerialString = true;
        if (!equalWithNull(userProperties.email, user.getEmail())) {
            user.setEmail(userProperties.email);
            keepSerialString = false;
        }
        if (!equalWithNull(userProperties.firstName, user.getFirstName())) {
            user.setFirstName(userProperties.firstName);
            keepSerialString =  false;
        }
        if (!equalWithNull(userProperties.lastName, user.getLastName())) {
            user.setLastName(userProperties.lastName);
            keepSerialString =  false;
        }
        if (!equalWithNull(userProperties.idpDisplayName, user.getIDPName())) {
            user.setIDPName(userProperties.idpDisplayName);
            keepSerialString =  false;
        }
        user.setDisplayName(userProperties.displayName);
        user.setAttr_json(userProperties.attr_json);
        user.setAffiliation(userProperties.affiliation);
        user.setOrganizationalUnit(userProperties.organizationalUnit);
        user.setUseUSinDN(userProperties.useUSinDNString);
        return keepSerialString;
    }

    /**
     * Finds the most recently created user with a specific {@link UserMultiID}.
     * <h3>Nota Bene</h3>
     * This should always work and return a unique user. SAML  requires
     * that the entity ID (aka dip parameter) restricts the domain of multi-id
     * (So no matter what the IDP lists, say umissouri vs mizzou, no other institution can
     * create an eppn with those domains.) If they should ever change that, then this might
     * not work right. Unlikely, but this contract is implicit and necessary.
     *
     * @param request
     * @param response
     * @param userMultiKey
     * @return
     * @throws IOException
     */
    protected User getUserNoIDP(HttpServletRequest request, HttpServletResponse response, UserMultiID userMultiKey) throws IOException {
        // Fix for CIL-1969 when no IDP is sent
        // Fix for https://jira.ncsa.illinois.edu/browse/CIL-2201
        // Updates logic for getUser when there is no IDP.
        Collection<User> users;
        User user = null;
        try {
            users = getUserStore().get(userMultiKey, null);
        } catch (UserNotFoundException userNotFoundException) {
            return null;
        }
        if (users == null || users.size() == 0) {
            return null;
        }
        for (User currentUser : users) {
            if (user == null) {
                user = currentUser;
            } else {
                // JIRA task specifies using the creation timestamp to differentiate these.
                if (user.getCreationTS().compareTo(currentUser.getCreationTS()) < 0) {
                    user = currentUser;
                }
            }
        }
        return user;
    }

    /**
     * Legacy code. This does all the logic for users who have their IDP sent. Note that this uniquely identifies
     * a user in the system, unlike the no IDP case. It will also make very complex logic decisions about whether the
     * user's serial string needs to be changed -- a requirement for X509 certificates.
     * <h2>DO NOT TOUCH</h2>
     * <i>... without <b>great</b> probity first...</i>
     *
     * @param request
     * @param response
     * @param idp
     * @throws IOException
     */
    protected void getUserWithIDP(HttpServletRequest request,
                                  HttpServletResponse response,
                                  UserMultiID userMultiKey,
                                  String idp) throws IOException {

        UserProperties userProperties = getUserProperties(request);

        if (!userMultiKey.isTrivial()) {
            // CIL-540
            // case 1.5 -- if the request has the IDP and one of eptid, eppn or oidc,
            // then this is sufficient to identify the user uniquely.
            User user = null;
            int status = STATUS_OK;

            try {
                user = findUser(userMultiKey, idp);

                // if found
                boolean keepSerialID = updateUser(user,
                        idp,
                        userProperties.email,
                        userProperties.firstName,
                        userProperties.lastName,
                        userProperties.idpDisplayName,
                        userMultiKey,
                        userProperties.affiliation,
                        userProperties.attr_json,
                        userProperties.displayName,
                        userProperties.organizationalUnit,
                        userProperties.useUSinDNString);
                status = (keepSerialID) ? STATUS_OK : STATUS_USER_SERIAL_STRING_UPDATED;
            } catch (EPTIDMismatchException | PairwiseIDMismatchException | SubjectIDMismatchException x) {
                throw x;
            } catch (UserNotFoundException unf) {
                // user has been uniquely identified. Make a new user and populate the object with whatever
                // was sent. There is no requirement for anything else.
                user = makeNewUser(response,
                        idp,
                        userProperties.email,
                        userProperties.firstName,
                        userProperties.lastName,
                        userProperties.idpDisplayName,
                        userMultiKey,
                        userProperties.affiliation,
                        userProperties.attr_json,
                        userProperties.displayName,
                        userProperties.organizationalUnit,
                        userProperties.useUSinDNString);
                status = STATUS_NEW_USER;
            }
            TwoFactorInfo tfi = get2FStore().get(user.getIdentifier());
            writeUser(user, tfi, status, response);
            return;
        }


        // case 2, use remote user to see if user has been updated or not.


        if (isEmpty(userProperties.idpDisplayName) && isEmpty(userProperties.firstName) && isEmpty(userProperties.lastName) && isEmpty(userProperties.email)) {
            ServletDebugUtil.trace(this, "Some value is empty, finding user by umk and idp");

            try {
                User user = findUser(userMultiKey, idp);

                user.setUseUSinDN(userProperties.useUSinDNString);

                TwoFactorInfo tfi = get2FStore().get(user.getIdentifier());
                ServletDebugUtil.trace(this, "found user " + user);
                if (user.isUseUSinDN() != userProperties.useUSinDNString) {
                    ServletDebugUtil.trace(this, "saving user");
                    // only actually save this if this changes.
                    getUserStore().save(user);
                    ServletDebugUtil.trace(this, "user after save = " + user);
                }

                writeUser(user, tfi, STATUS_OK, response);
                return;
            } catch (UserNotFoundException x) {
                User user = makeNewUser(response, idp,
                        userProperties.email,
                        userProperties.firstName,
                        userProperties.lastName,
                        userProperties.idpDisplayName,
                        userMultiKey, userProperties.affiliation,
                        userProperties.attr_json,
                        userProperties.displayName,
                        userProperties.organizationalUnit,
                        userProperties.useUSinDNString);
                writeUser(user, STATUS_NEW_USER, response);
                return;
            }
        }
        try {
            // check that the user is valid and if something has changed, archive the user's old information.
            // user id's are immutable, so this will not create a new one, though it will create a new archived user id.
            ServletDebugUtil.trace(this, "Checking and maybe archiving user");
            if (userProperties.firstName == null) {
                System.err.println("got a null first name");
            }
            checkAndArchiveUser(response,
                    userMultiKey,
                    idp,
                    userProperties.idpDisplayName,
                    userProperties.firstName,
                    userProperties.lastName,
                    userProperties.email,
                    userProperties.affiliation,
                    userProperties.displayName,
                    userProperties.organizationalUnit,
                    userProperties.useUSinDNString,
                    userProperties.attr_json);
        } catch (UserNotFoundException unfx) {

            // case 3: no such user, create one.
            ServletDebugUtil.trace(this, "'case 3' no user found so searching for id to create one");

            info("WRITING NEW USER");
            User user3 = null;
            boolean gotOne = false;
            InvalidUserIdException lastX = null;
            for (int i = 0; i < getCILSE().getMaxUserIdRetries(); i++) {

                if (gotOne) break;
                try {
                    user3 = getUserStore().createAndRegisterUser(userMultiKey,
                            idp,
                            userProperties.idpDisplayName,
                            userProperties.firstName,
                            userProperties.lastName,
                            userProperties.email,
                            userProperties.affiliation,
                            userProperties.displayName,
                            userProperties.organizationalUnit);
                    user3.setUseUSinDN(userProperties.useUSinDNString);
                    user3.setAttr_json(userProperties.attr_json);
                    ServletDebugUtil.trace(this, "created user " + user3);

                    //CIL-503 fix:
                    getUserStore().updateCheckSerialString(user3, true);
                    gotOne = true;
                } catch (InvalidUserIdException iuidx) {
                    // keep retying.
                    lastX = iuidx;
                }
            }
            if (!gotOne) {
                if (lastX != null) {
                    throw lastX;
                }
                throw new InvalidUserIdException("Error: Could not find an unused user id. Aborting...");
            }
            // it is possible that there might be some information already about this user. Check, just in case...
            TwoFactorInfo tfi = get2FStore().get(user3.getIdentifier());
            writeUser(user3, tfi, STATUS_NEW_USER, response);
            info("DONE WRITING NEW USER, ID = " + user3.getIdentifier());
        }
    }


    private UserProperties getUserProperties(HttpServletRequest request) throws UnsupportedEncodingException {
        String email = getParam(request, userKeys.email(), true);
        String firstName = getParam(request, userKeys.firstName(), true);
        String lastName = getParam(request, userKeys.lastName(), true);
        String idpDisplayName = getParam(request, userKeys.idpDisplayName(), true);

        String affiliation = request.getParameter(userKeys.affiliation());
        String attr_json = request.getParameter(userKeys.attr_json());
        String displayName = request.getParameter(userKeys.displayName());
        String organizationalUnit = request.getParameter(userKeys.organizationalUnit());

        String isUSDN = getParam(request, userKeys.useUSinDN(), true);
        boolean useUSinDN = true;
        if (isUSDN != null) {
            useUSinDN = parseUseUSinDNString(isUSDN);
        }

        UserProperties userProperties = new UserProperties(email, firstName, lastName, idpDisplayName, affiliation, attr_json, displayName, organizationalUnit, useUSinDN);
        return userProperties;
    }

    private static class UserProperties {
        public final String email;
        public final String firstName;
        public final String lastName;
        public final String idpDisplayName;
        public final String affiliation;
        public final String attr_json;
        public final String displayName;
        public final String organizationalUnit;
        public final boolean useUSinDNString;

        public UserProperties(String email, String firstName, String lastName, String idpDisplayName,
                              String affiliation, String attr_json, String displayName, String organizationalUnit,
                              boolean useUSinDNString) {
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.idpDisplayName = idpDisplayName;
            this.affiliation = affiliation;
            this.attr_json = attr_json;
            this.displayName = displayName;
            this.organizationalUnit = organizationalUnit;
            this.useUSinDNString = useUSinDNString;
        }
    }

    /**
     * Populates a user object and saves it. It takes more time to check if fields are set than to just save
     * it every time.
     *
     * @param user
     * @param idp
     * @param entityID
     * @param affiliation
     * @param attr_json
     * @param displayName
     * @param organizationalUnit
     * @param useUSinDN
     * @return keepSerialID
     * @throws IOException
     */
    private boolean updateUser(User user,
                               String idp,
                               String email,
                               String firstName,
                               String lastName,
                               String idpDisplayName,
                               UserMultiID entityID,
                               String affiliation,
                               String attr_json,
                               String displayName,
                               String organizationalUnit,
                               Boolean useUSinDN) throws IOException {
        DNState newDNState = new DNState();

        boolean oldCanGetCert = user.canGetCert();
        if (email != null && !email.equals(user.getEmail())) {
            newDNState.setEmail(true);
            user.setEmail(email);
        }
        if (firstName != null && !firstName.equals(user.getFirstName())) {
            newDNState.setFirstName(true);
            user.setFirstName(firstName);
        }
        if (lastName != null && !lastName.equals(user.getLastName())) {
            newDNState.setLastName(true);
            user.setLastName(lastName);
        }
        if (idpDisplayName != null && !idpDisplayName.equals(user.getIDPName())) {
            newDNState.setIDPName(true);
            user.setIDPName(idpDisplayName);
        }
        if (displayName != null && !displayName.equals(user.getDisplayName())) {
            newDNState.setDisplayName(true);
            user.setDisplayName(displayName);
        }

        if (affiliation != null) {
            user.setAffiliation(affiliation);
        }

        if (organizationalUnit != null) {
            user.setOrganizationalUnit(organizationalUnit);
        }

        if (!isEmpty(attr_json)) {
            user.setAttr_json(attr_json);
        }

        UserMultiID newID = user.getUserMultiKey().union(entityID);
        DNState userDNState = user.getDNState();
        // This tells us if we have to change the serial string based in the user attribues.
        BitSet bs = new BitSet();
        bs.or(userDNState.getBitSet());
        bs.or(newDNState.getBitSet());
        DNState resultant = new DNState(bs);
        boolean keepSerialString = true;
        if (!oldCanGetCert) {
            // Case is that a bunch of new info was uploaded.
            // We ONLY store one way to create a DN and use that in all subsequent calls.
            // Here is where we ferret out which to use and set it up. This gets called at
            // most once per user ever.
            if (resultant.hasEmail() && resultant.hasIDPName()) {
                if (resultant.hasFirstName() && resultant.hasLastName()) {
                    newDNState.setStateValue(newDNState.valid_flName); // unset the display name. Again, this is a policy decision.
                    user.setDNState(newDNState); // use the setter or the underlying JSON state object is not altered.
                    keepSerialString = true;
                } else {
                    if (resultant.hasDisplayName()) {
                        newDNState.setStateValue(newDNState.valid_dName); // Set only display name.
                        user.setDNState(newDNState);
                        keepSerialString = true;
                    }
                }
            }
            // option B is that new there is finally enough information.
        } else {
            keepSerialString = userDNState.keepSerialString(newDNState);
        }

        if (!newID.equals(user.getUserMultiKey())) {
            // Only keep ithe serial string if everyone agrees we should.
            keepSerialString = keepSerialString && user.getUserMultiKey().keepSerialString(newID);
            user.setUserMultiKey(newID);
        }
        if (idp != null) {
            // CIL-1969
            user.setIdP(idp); // Does **not** trigger a serial string update!
        }
        user.setUseUSinDN(useUSinDN);
        // Again, the second argument in the next call  means
        ServletDebugUtil.trace(this, "saved user " + user);
        if ((!oldCanGetCert && user.canGetCert()) || (!user.canGetCert())) {
            getUserStore().updateCheckSerialString(user, true); // force that there is no new serial string produced.
            // user could not get a cert before but can no, don't change serial string, use current.
            return true;
        }
        // If we get here, then the user has been able to create certs and something may or may not have changed.
        getUserStore().updateCheckSerialString(user, keepSerialString);
        return keepSerialString;
    }

    /**
     * Create a new user in the store and populate the object.
     *
     * @param response
     * @param idp
     * @param names
     * @param affiliation
     * @param attr_json
     * @param displayName
     * @param organizationalUnit
     * @param useUSinDN
     * @throws IOException
     */
    private User makeNewUser(HttpServletResponse response, String idp,
                             String email,
                             String firstName,
                             String lastName,
                             String idpDisplayName,
                             UserMultiID names,
                             String affiliation,
                             String attr_json,
                             String displayName,
                             String organizationalUnit,
                             Boolean useUSinDN) throws IOException {
        ServletDebugUtil.trace(this, "No user found, creating a new user");

        // spec says to create a new user if none is found for the given identifier.
        User user = getUserStore().create(true);
        getUserStore().register(user); // or you will get an unregistered object exception.
        ServletDebugUtil.trace(this, "created user " + user);
        updateUser(user,
                idp,
                email,
                firstName,
                lastName,
                idpDisplayName,
                names,
                affiliation,
                attr_json,
                displayName,
                organizationalUnit,
                useUSinDN);
        return user;
    }


    protected void getUserID(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserMultiID userMultiKey = getNames(request);
        String idp = getParam(request, userKeys.idp());
        Identifier userid = null;
        userid = getUserStore().getUserID(userMultiKey, idp);
        startWrite(response);
        serializer.serialize(response.getWriter(), userid);
        stopWrite(response);
    }

    /**
     * Gets the parameter for the given key, decoding it as needed.
     *
     * @param request
     * @param key
     * @param nullOK
     * @return
     * @throws java.io.UnsupportedEncodingException
     */
    protected String getParam(HttpServletRequest request, String key, boolean nullOK) throws UnsupportedEncodingException {
        request.setCharacterEncoding("UTF-8");
        String[] params = request.getParameterValues(key);
        if (null == params || params.length == 0) {
            if (nullOK) {
                return null;
            }
            info("Error: missing parameter for key \"" + key + "\"");
            throw new DBServiceException(STATUS_MISSING_ARGUMENT);
        }
        if (1 < params.length) {
            info("Error: duplicate parameter for key \"" + key + "\"");
            throw new DBServiceException(STATUS_DUPLICATE_ARGUMENT);
        }
        return params[0];

    }


    /**
     * Gets a single parameter, throwing the appropriate exception if there are multiples or none
     *
     * @param key
     * @return
     */
    protected String getParam(HttpServletRequest request, String key) throws UnsupportedEncodingException {
        return getParam(request, key, false);
    }

    /**
     * Fetch the transaction by authorization grant only
     *
     * @param request
     * @param response
     * @throws IOException
     * @throws javax.servlet.ServletException
     */
    public void getPortalParameter(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        AuthorizationGrant ag = getTokenForge().getAuthorizationGrant(getParam(request, getServiceEnvironment().getConstants().get(ServiceConstantKeys.TOKEN_KEY)), null);
        if (ag == null) {
            getMyLogger().warn("No token found. Cannot retrieve transaction");
            throw new DBServiceException(StatusCodes.STATUS_TRANSACTION_NOT_FOUND);
        }

        ServiceTransaction t = getTransaction(ag);
        if (t == null) {
            getMyLogger().warn("Did not find transaction w/token =" + ag);
            throw new DBServiceException(StatusCodes.STATUS_TRANSACTION_NOT_FOUND);
        }


        if (t.getClient() == null) {

            if (getTransactionStore() instanceof CILSQLTransactionStore) {
                String identifier = t.getIdentifierString();
                info("Trying to get legacy info for client with identifier " + identifier);
                CILSQLTransactionStore transactionStore = (CILSQLTransactionStore) getTransactionStore();
                ConnectionRecord cr = transactionStore.getConnection();
                Connection c = cr.connection;

                String statement = "SELECT portal_name,success_uri,error_uri from cilogon.a_transaction where temp_cred=?";
                try {
                    PreparedStatement stmt = c.prepareStatement(statement);
                    stmt.setString(1, identifier);
                    stmt.executeQuery();
                    ResultSet rs = stmt.getResultSet();
                    if (!rs.next()) {
                        info("no result found for legacy client " + identifier);
                        rs.close();
                        stmt.close();
                        throw new TransactionNotFoundException("Legacy client: no transaction found for identifier \"" + identifier + "\"");
                    }
                    AnonymousClient ac = new AnonymousClient();
                    ac.setName(rs.getString("portal_name"));
                    ac.setHomeUri(rs.getString("success_uri"));
                    ac.setErrorUri(rs.getString("error_uri"));
                    t.setClient(ac); //don't save this, since the identifier is not unique!
                    rs.close();
                    stmt.close();
                    transactionStore.releaseConnection(cr);
                    info("Got legacy client info for " + identifier);
                } catch (SQLException sqlx) {
                    sqlx.printStackTrace();
                    info("No legacy client retrieved for transaction " + identifier);
                    // last ditch effort... see if the old service can be pinged.
                    //legacyPP(ag, response);
                    return;
                }
            }
            // try and get it from the old service. Can't do much about it.
            // NOTE that this pre-supposes that the new and old service share a store for transactions.
            // If that is not the case, none of this will work.
        }

        startWrite(response);
        serializer.serialize(response.getWriter(), t);
        stopWrite(response);
    }

    Identifier ANONYMOUS = BasicIdentifier.newID("cilogon:client:anonymous");

    class AnonymousClient extends Client {
        AnonymousClient() {
            super(ANONYMOUS);
            setProxyLimited(false);
            setSecret("anonymous");
        }
    }


    protected int lookupCase(String x) {
        if (x.equals(GET_USER)) return GET_USER_CASE;
        if (x.equals(CREATE_USER)) return CREATE_USER_CASE;
        if (x.equals(GET_USER_ID)) return GET_USER_ID_CASE;
        if (x.equals(GET_PORTAL_PARAMETER)) return GET_PORTAL_PARAMETER_CASE;
        if (x.equals(GET_LAST_ARCHIVED_USER)) return GET_LAST_ARCHIVED_USER_CASE;
        if (x.equals(HAS_USER)) return HAS_USER_CASE;
        if (x.equals(REMOVE_USER)) return REMOVE_USER_CASE;
        if (x.equals(UPDATE_USER)) return UPDATE_USER_CASE;
        if (x.equals(GET_ALL_IDPS)) return GET_ALL_IDPS_CASE;
        if (x.equals(SET_ALL_IDPS)) return SET_ALL_IDPS_CASE;
        if (x.equals(GET_TWO_FACTOR_INFO)) return GET_TWO_FACTOR_INFO_CASE;
        if (x.equals(SET_TWO_FACTOR_INFO)) return SET_TWO_FACTOR_INFO_CASE;
        return UNRECOGNIZED_CASE;
    }

    /**
     * Create a user.
     *
     * @param request
     * @param response
     * @throws IOException
     */

    protected void createUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserMultiID names = getNames(request);
        String idp = getParam(request, userKeys.idp());
        try {
            getUserStore().getUserID(names, idp);
            getMyLogger().warn("Create user fails: user exists for " + names + ", and idp=" + idp);
            throw new DBServiceException(STATUS_USER_EXISTS_ERROR);
        } catch (UserNotFoundException x) {
            // this is what we want. This means we are creating a user.
        }
        // The rest of these might be missing.
        String email = getParam(request, userKeys.email(), true);
        String firstName = getParam(request, userKeys.firstName, true);
        String lastName = getParam(request, userKeys.lastName(), true);
        String idpCol = getParam(request, userKeys.idpDisplayName(), true);
        String affiliation = getParam(request, userKeys.affiliation(), true);
        String displayName = getParam(request, userKeys.displayName(), true);
        String organizationalUnit = getParam(request, userKeys.organizationalUnit(), true);
        String useUSinDNString = getParam(request, userKeys.useUSinDN(), true);
        User user = getUserStore().createAndRegisterUser(names,
                idp,
                idpCol,
                firstName,
                lastName,
                email,
                affiliation,
                displayName,
                organizationalUnit);
        ServletDebugUtil.trace(this, "created user. uid=" + user.getIdentifierString() + ", serial string = " + user.getSerialString());
        if (useUSinDNString != null) {
            user.setUseUSinDN(parseUseUSinDNString(useUSinDNString));
        }
        getUserStore().updateCheckSerialString(user, true);
        ServletDebugUtil.trace(this, "stored user. uid=" + user.getIdentifierString() + ", serial string = " + user.getSerialString());

        writeUser(user, STATUS_NEW_USER, response);
    }

    /**
     * Handles parsing the "use in us " string, which is either absent or the strings
     * "0" or "1"
     *
     * @param useUSinDN
     * @return
     */
    protected Boolean parseUseUSinDNString(String useUSinDN) {
        if (useUSinDN == null) {
            return null;
        }
        if (useUSinDN.equals("0")) return false;
        if (useUSinDN.equals("1")) return true;
        throw new IllegalArgumentException("Error: illegal value for us_idp parameter: \"" + useUSinDN + "\"");
    }

    protected void hasUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // this parameter might be missing, so we have to allow for that. If present, it has priority over other parameters
        String useruidString = getParam(request, userKeys.identifier(), true);
        boolean hasUser = false;
        if (isEmpty(useruidString)) {
            UserMultiID userMultiKey = getNames(request);
            String idp = getParam(request, userKeys.idp());
            try {
                getUserStore().get(userMultiKey, idp);
                hasUser = true;
            } catch (UserNotFoundException x) {
                hasUser = false;
            }
        } else {
            Identifier uid = newID(useruidString);
            hasUser = getUserStore().containsKey(uid);
        }
        writeMessage(response, hasUser ? STATUS_USER_EXISTS : STATUS_USER_NOT_FOUND); // these are not error, just info.
    }

    protected User findUser(UserMultiID userMultiKey, String idp) throws IOException {
        if (isTrivial(idp)) {
            NFWException nfwException = new NFWException("Trivial IDP found"); // stop this cold if it ever happens.
            nfwException.printStackTrace();
            throw nfwException;
        }
        return OLDfindUser(userMultiKey, idp);
    }


    protected User OLDfindUser(UserMultiID userMultiKey, String idp) throws IOException {
        Collection<User> users = getUserStore().get(userMultiKey, idp);
        boolean idpFail = false;
        User user = null;
        for (User currentUser : users) {
            if (!currentUser.getIdP().equals(idp)) {
                if (currentUser.hasEPTID() || currentUser.hasPairwiseID() || currentUser.hasSubjectID()) {
                    // Fail immediately. Pairwise id, subject ids and EPTIDs are globally unique so the IDP must match.
                    throw new GeneralException("Error: unexpected idp encountered. Expected idp=" + idp + ", got " + currentUser.getIdP() + " for key=" + userMultiKey);
                }
                idpFail = true;
                continue;
            }
            userLogic(currentUser, userMultiKey);
            getUserStore().updateCheckSerialString(currentUser, true);
            if (user == null) {
                user = currentUser;
            } else {
                if (user.getCreationTS().compareTo(currentUser.getCreationTS()) < 0) {
                    user = currentUser;
                }
            }
        }
        if (user == null && idpFail) {
            throw new GeneralException("Error: unexpected idp encountered. Expected " + idp + "for key=" + userMultiKey);
        }
        return user;
    }

    /**
     * This method tries to determine what id to use to find the user. There are a few edge cases. This returns
     * a true if the user was updated so that any changes can be saved.
     *
     * @param u
     * @param k
     */
    protected void userLogic(User u, UserMultiID k) {
        // case 0. All of the ids are set. This should be flagged as an error condition.
        if (k.hasRemoteUser() && k.hasEPTID() && k.hasEPPN() && k.hasOpenID()) {
            throw new IllegalArgumentException("Error: All ids, eppn, eptid, openid and remote user are set. Request rejected.");
        }

        // OIDC is unique. OpenID is not. Therefore, if the user has OIDC changing it effectively changes the user.
        if (k.hasOpenIDConnect()) {
            if (k.hasOpenID()) {
                u.setOpenID(k.getOpenID());
                if (u.hasOpenIDConnect()) {
                    if (!u.getOpenIDConnect().equals(k.getOpenIDConnect())) {
                        throw new NFWException("Error: both the user and the requested key have different Open ID Connect identifier. " +
                                "This indicates an internally inconsistent state in this user.");
                    }
                } else {
                    // ok. So the key has an OIDC and the user does not. Fix that.
                    u.setOpenIDConnect(k.getOpenIDConnect());
                }
            }
        }
        // case 1: open ID sent.
        if (k.hasOpenID()) {
            if (u.hasOpenID()) {
                if (!k.getOpenID().equals(u.getOpenID())) {
                    info("Note that user " + u.getIdentifierString() + " had openID=" + u.getOpenID() + ", which was changed to " + k.getOpenID());
                    u.setOpenID(k.getOpenID());
                }
            } else {
                u.setOpenID(k.getOpenID());
            }
            return;
        }

        // case 2: Check the EPTID since that is always globally unique.
        // NOTE if both EPTID or pairwise or subject  and EPPN were sent, this should prefer EPPN. Hence no return in other cases.

        if (k.hasEPTID()) {
            if (!u.hasEPTID()) {
                u.setePTID(k.getEptid());
            }
            if (k.hasEPPN()) {
                u.setePPN(k.getEppn());
                if (u.hasEPTID()) {
                    if (!u.getePTID().equals(k.getEptid())) {
                        throw new EPTIDMismatchException("Error: both the user and the requested key have different EPTIDs " +
                                "This indicates an internally inconsistent state in this user.");
                    }
                }
            }
        }

        if (k.hasPairwiseID()) {
            if (!u.hasPairwiseID()) {
                u.setPairwiseId(k.getPairwiseID());
            }
            if (k.hasEPPN()) {
                u.setePPN(k.getEppn());
                if (u.hasPairwiseID()) {
                    if (!u.getPairwiseID().equals(k.getPairwiseID())) {
                        throw new PairwiseIDMismatchException("Error: both the user and the requested key have different  pairwise IDs " +
                                "This indicates an internally inconsistent state in this user.");
                    }
                }
            }
        }

        if (k.hasSubjectID()) {
            if (!u.hasSubjectID()) {
                u.setSubjectId(k.getSubjectID());
            }
            if (k.hasEPPN()) {
                u.setePPN(k.getEppn());
                if (u.hasSubjectID()) {
                    if (!u.getSubjectID().equals(k.getSubjectID())) {
                        throw new SubjectIDMismatchException("Error: both the user and the requested key have different  subject IDs " +
                                "This indicates an internally inconsistent state in this user.");
                    }
                }
            }
        }

        // case 3: EPPN (which is not unique) is sent, but not EPTID

        if (k.hasEPPN()) {
            if (u.hasEPPN()) {
                if (!k.getEppn().equals(u.getePPN())) {
                    info("Note that user " + u.getIdentifierString() + " had EPPN=" + u.getePPN() + ", which was changed to " + k.getEppn());
                    u.setePPN(k.getEppn());
                }
            } else {
                u.setePPN(k.getEppn());
            }
            return;
        }

    }

    /**
     * Takes all 6 parameters and an existing user. Checks that the new values differ from the old and if
     * so, archives the user. writes either the user (if no change) or the updated user.
     *
     * @param response
     * @param userMultiKey
     * @param idp
     * @param idpDisplayName
     * @param firstName
     * @param lastName
     * @param email
     * @throws IOException
     */

    protected void checkAndArchiveUser(HttpServletResponse response,
                                       UserMultiID userMultiKey,
                                       String idp,
                                       String idpDisplayName,
                                       String firstName,
                                       String lastName,
                                       String email,
                                       String affiliation,
                                       String displayName,
                                       String organizationalUnit,
                                       Boolean useUSinDN,
                                       String memberOf) throws IOException {

        User oldUser = findUser(userMultiKey, idp);
        ServletDebugUtil.trace(this, "get&Archive: found user=" + oldUser);
        TwoFactorInfo tfi = get2FStore().get(oldUser.getIdentifier());
        if (oldUser.compare(idpDisplayName, firstName, lastName, email)) {
            info("No change to user \"" + oldUser.getIdentifier() + "\", returning");

            // There is an issue. This condition is to trigger an archive user event. The affiliation &c. can still change,
            // which should cause the user to be updated, but should not cause the user to be archived.
            boolean saveUser = false;
            if (!checkEquals(affiliation, oldUser.getAffiliation())) {
                oldUser.setAffiliation(affiliation);
                saveUser = true;
            }
            if (!checkEquals(organizationalUnit, oldUser.getOrganizationalUnit())) {
                oldUser.setOrganizationalUnit(organizationalUnit);
                saveUser = true;
            }
            if (!checkEquals(memberOf, oldUser.getAttr_json())) {
                oldUser.setAttr_json(memberOf);
                saveUser = true;
            }

            if (!checkEquals(displayName, oldUser.getDisplayName())) {
                oldUser.setDisplayName(displayName);
                saveUser = true;
            }
            if (useUSinDN != null && oldUser.isUseUSinDN() != useUSinDN) {
                oldUser.setUseUSinDN(useUSinDN);
                saveUser = true;
            }
            if (saveUser) {
                ServletDebugUtil.trace(this, "get&Archive: Saving user, no update");

                getUserStore().updateCheckSerialString(oldUser, true); // force that there is no new serial string produced.
            }
            writeUser(oldUser, tfi, STATUS_OK, response);
            return;
        }
        info("Archiving user \"" + oldUser.getIdentifier() + "\", returning");
        ServletDebugUtil.trace(this, "get&Archive: archiving user");


        getArchivedUserStore().archiveUser(oldUser.getIdentifier());
        ServletDebugUtil.trace(this, "get & Archive: after archiving user " + oldUser);

        // Now update to the new values and save it.
        oldUser.setIDPName(idpDisplayName);
        oldUser.setFirstName(firstName);
        oldUser.setLastName(lastName);
        oldUser.setEmail(email);
        oldUser.setAffiliation(affiliation);
        oldUser.setDisplayName(displayName);
        oldUser.setOrganizationalUnit(organizationalUnit);
        if (useUSinDN != null) {
            oldUser.setUseUSinDN(useUSinDN);
        }
        if (!isEmpty(memberOf)) {
            oldUser.setAttr_json(memberOf);
        }
        getUserStore().update(oldUser);
        ServletDebugUtil.trace(this, "get&Archive: updated user =" + oldUser);


        writeUser(oldUser, tfi, STATUS_USER_SERIAL_STRING_UPDATED, response);
        return;
    }

    protected boolean isEmpty(String x) {
        return x == null || x.length() == 0;
    }

    /**
     * One issue is that we may be getting a value for a string to be null or just the empty string
     * depending upon the source (backing stores may do different things). Therefore we should treat
     * these possibilities as symmetric.
     *
     * @param x
     * @param y
     * @return
     */
    protected boolean checkEquals(String x, String y) {
        if (isEmpty(x)) {
            if (isEmpty(y)) {
                return true;
            } else {

                return y.equals(x);
            }
        } else {
            if (isEmpty(y)) {
                if (isEmpty(x)) {
                    return true;
                }
                return false;
            }
        }
        return x.equals(y);
    }


    public void updateUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserMultiID userMultiKey = getNames(request);
        // The rest of these might be missing.
        String idp = getParam(request, userKeys.idp(), false);
        UserProperties userProperties = getUserProperties(request);
        String email = getParam(request, userKeys.email(), true);
        String firstName = getParam(request, userKeys.firstName(), true);
        String lastName = getParam(request, userKeys.lastName(), true);
        String idpDisplayName = getParam(request, userKeys.idpDisplayName(), true);
        String affiliation = getParam(request, userKeys.affiliation(), true);
        String displayName = getParam(request, userKeys.displayName(), true);
        String organizationalUnit = getParam(request, userKeys.organizationalUnit(), true);
        String useUSinDNString = getParam(request, userKeys.useUSinDN(), true);
        String memberOf = getParam(request, userKeys.attr_json(), true);
        boolean useUSinDN = true;
        if (useUSinDNString != null) {
            useUSinDN = parseUseUSinDNString(useUSinDNString);
        }
        checkAndArchiveUser(response,
                userMultiKey,
                idp,
                idpDisplayName,
                firstName,
                lastName,
                email,
                affiliation,
                displayName,
                organizationalUnit,
                useUSinDN,
                memberOf);
    }


    /**
     * This would be better names as <code>addIdps</code> since it does not remove any IDPs. In point of
     * fact, it will make a diff of the argument and what is currently stored. Only new entries are stored.
     * This deletes nothing ever. The name is left from an earlier version
     *
     * @param request
     * @param response
     * @throws IOException
     */
    public void setAllIdps(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String[] idpStrings = request.getParameterValues(idpKeys.identifier());

        // Don't save an empty list. This would clear the entire list and is probably a mistake anyway
        if (idpStrings == null || idpStrings.length == 0) {
            throw new DBServiceException(STATUS_IDP_SAVE_FAILED);
        }


        Collection<IdentityProvider> newIDPS = new LinkedList<>();
        for (String idp : idpStrings) {
            newIDPS.add(new IdentityProvider(BasicIdentifier.newID(idp)));
        }
        getIDPStore().add(newIDPS);
        setStatusOK(response);
    }


    public void getAllIdps(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Collection<IdentityProvider> idps = getIDPStore().values();
        // now we have to stick these into the response
        startWrite(response);
        serializer.serialize(response.getWriter(), idps);
        stopWrite(response);
    }

    protected void write2FInfo(TwoFactorInfo tfi, int statusCode, HttpServletResponse response) throws IOException {
        startWrite(response);
        serializer.serialize(response.getWriter(), tfi, statusCode);
        stopWrite(response);
    }

    /**
     * Utility to take a user and put it in the response, with all the appropriate fields.
     *
     * @param user
     * @param response
     */
    protected void writeUser(User user, int statusCode, HttpServletResponse response) throws IOException {
        startWrite(response);
        serializer.serialize(response.getWriter(), user, statusCode);
        stopWrite(response);
    }


    protected void writeUser(User user, TwoFactorInfo tfi, int statusCode, HttpServletResponse response) throws IOException {
        startWrite(response);
        serializer.serialize(response.getWriter(), user, tfi, statusCode);
        stopWrite(response);
    }

    /**
     * Does nothing except set the status ok and close out the response. Use this if you only
     * need to report that the operation was a success.
     *
     * @param response
     */
    protected void setStatusOK(HttpServletResponse response) throws IOException {
        writeMessage(response, STATUS_OK);
    }

    public void getLastArchivedUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Identifier userid = newID(request.getParameter(userKeys.identifier()));
        ArchivedUser lastOne = getArchivedUserStore().getLastArchivedUser(userid);
        if (lastOne == null) {
            // None of these have been archived. We *could* check if the user has a valid uid
            // in the store and return user not found if so and user not found error if not,
            // but that would be messier to use. If this is even an issue
            writeMessage(response, STATUS_USER_NOT_FOUND_ERROR);
            return;
        }
        // the last of these is the last archived user. Note that the returned list is always sorted,
        // so we just grab the last one.
        writeUser(lastOne.getUser(), STATUS_OK, response);
    }

    /**
     * If there is an exception, this writes the corresponding message to the response.
     *
     * @param response
     * @param statusCode
     * @throws IOException
     */
    protected void writeMessage(HttpServletResponse response, int statusCode) throws IOException {
        if (statusCode != STATUS_OK) {
            // track in debugging when a non-success is returned.
            ServletDebugUtil.trace(this, "Serialization error of " + statusCode + " (0x" + Long.toHexString(statusCode).toUpperCase() + ")");
        }
        writeMessage(response, Integer.toString(statusCode));
    }


    protected void writeMessage(HttpServletResponse response, String statusMessage) throws IOException {
        startWrite(response);
        serializer.writeMessage(response.getWriter(), statusMessage);
        stopWrite(response);
    }


    /**
     * Sets up the response with the right encoding and status.
     *
     * @param response
     */
    protected void startWrite(HttpServletResponse response) {
        response.setContentType(FORM_ENCODING_KEY);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(SC_OK);
    }

    /**
     * Stop writing to the response. This flushes and closes the writer. No writes should work after this.
     *
     * @param response
     */
    protected void stopWrite(HttpServletResponse response) throws IOException {
        response.getWriter().flush();
        response.getWriter().close();
    }

    /**
     * Empty method. Since this servlet extends {@link OA4MPServlet} this method is
     * required, but there is nothing to verify, since this servlet does not issue {@link IssuerResponse}'s.
     *
     * @param iResponse@return
     * @return
     * @throws IOException
     */
    @Override
    public ServiceTransaction verifyAndGet(IssuerResponse iResponse) throws IOException {
        return null;
    }
}
