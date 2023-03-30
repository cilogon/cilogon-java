package test.cilogon;

import edu.uiuc.ncsa.myproxy.oa4mp.server.ServiceConstantKeys;
import edu.uiuc.ncsa.oa4mp.delegation.common.storage.TransactionStore;
import edu.uiuc.ncsa.oa4mp.delegation.server.storage.ClientStore;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.core.util.DateUtils;
import edu.uiuc.ncsa.security.storage.XMLMap;
import edu.uiuc.ncsa.security.util.TestBase;
import org.cilogon.oauth2.servlet.storage.idp.IdentityProvider;
import org.cilogon.oauth2.servlet.storage.idp.IdentityProviderStore;
import org.cilogon.oauth2.servlet.storage.user.*;
import org.cilogon.oauth2.servlet.twofactor.TwoFactorStore;
import org.cilogon.oauth2.servlet.util.ArchivedUserStore;
import org.cilogon.oauth2.servlet.util.DBServiceClient;
import org.cilogon.oauth2.servlet.util.UserKeys;

import java.util.Map;

import static org.cilogon.oauth2.servlet.StatusCodes.STATUS_OK;
import static org.cilogon.oauth2.servlet.servlet.AbstractDBService.STATUS_KEY;


/**
 * Periodically run this test. You must start up the service as indicated in the host string
 * below beforehand.
 * <H3>How to run this test</H3>
 * You must ensure that this test has a provider that points to the exact same store as the webapp, since
 * this test checks integrity directly against the store.<br>
 * You must also deploy the webapp locally at the address below in the field host = {@value #host}.<br>
 * Start the webapp, then start this running. The tests are extremely thorough and problems usually relate to
 * not having the right store (such as misconfiguring the environment).
 * <p>Created by Jeff Gaynor<br>
 * on Nov 15, 2010 at  12:46:49 PM
 */
public abstract class RemoteDBServiceTest extends TestBase {

    public static String getHost() {
        return host;
    }

    public static void setHost(String host) {
        RemoteDBServiceTest.host = host;
    }

 //  public static String host = "http://localhost:44444/oauth2/dbService";
    public static String host = "https://localhost:9443/oauth2/dbService";
    DBServiceClient dbsClient = null;

    public DBServiceClient getDBSClient() {
        if (dbsClient == null) {
            dbsClient = new DBServiceClient(getHost(), (String) getTSProvider().getConfigLoader().getConstants().get(ServiceConstantKeys.TOKEN_KEY));
        }
        return dbsClient;
    }


    UserKeys userKeys = new UserKeys();

    void checkUserAgainstMap(XMLMap map, User user, boolean checkSerialString) {
        assert user.getIdentifierString().equals(map.get(userKeys.identifier())) : "User ids don't match. Expected " + user.getIdentifierString() + ", got " + map.get(userKeys.identifier());
        if (user.hasRemoteUser()) {
            assert user.getRemoteUser().getName().equals(map.get(userKeys.remoteUser())) : "remote user fields don't match";
        } else {
            assert map.get(userKeys.remoteUser()) == null : "remote user fields do not match. User has no remote user, but map has \"" + map.get(userKeys.remoteUser()) + "\"";
        }
        if (user.hasEPPN()) {
            assert user.getePPN().getName().equals(map.get(userKeys.eppn())) : "eppn fields don't match";
        } else {
            assert map.get(userKeys.eppn()) == null : "eppn fields do not match.";
        }
        if (user.hasEPTID()) {
            assert user.getePTID().getName().equals(map.get(userKeys.eptid())) : "eptid fields don't match";
        } else {
            assert map.get(userKeys.eptid()) == null : "eptid fields do not match.";
        }
        if (user.hasOpenID()) {
            assert user.getOpenID().getName().equals(map.get(userKeys.openID())) : "open ID fields don't match";
        } else {
            assert map.get(userKeys.openID()) == null : "open ID fields do not match.";
        }
        if (user.hasOpenIDConnect()) {
            assert user.getOpenIDConnect().getName().equals(map.get(userKeys.oidc())) : "open ID Connect fields don't match. Expected " + user.getOpenIDConnect().getName() + " and got " + map.get(userKeys.oidc());
        } else {
            assert map.get(userKeys.oidc()) == null : "open ID Connect fields do not match. Expected null and got \"" + map.get(userKeys.oidc()) + "\"";
        }
        if (user.hasSubjectID()) {
            assert user.getSubjectID().getName().equals(map.get(userKeys.subjectId())) : "Subject ID  fields don't match. Expected "
                    + user.getSubjectID().getName() + " and got " + map.get(userKeys.subjectId());
        } else {
            assert map.get(userKeys.subjectId()) == null : "Subject ID  fields do not match. Expected null and got \""
                    + map.get(userKeys.subjectId()) + "\"";
        }
        if (user.hasPairwiseID()) {
            assert user.getPairwiseID().getName().equals(map.get(userKeys.pairwiseId())) : "Pairwise ID fields don't match. Expected " +
                    user.getPairwiseID().getName() + " and got " + map.get(userKeys.pairwiseId());
        } else {
            assert map.get(userKeys.pairwiseId()) == null : "Pairwise ID fields do not match. Expected null and got \""
                    + map.get(userKeys.pairwiseId()) + "\"";
        }

        assert user.getIdP().equals(map.get(userKeys.idp())) : "IDP's don't match";  // required
        if (!isEmpty(user.getIDPName())) {
            assert user.getIDPName().equals(map.get(userKeys.idpDisplayName())) : "IDP display names don't match. Expected \"" + user.getIDPName() + "\" and got \"" + (map.get(userKeys.idpDisplayName())) + "\"";
        }
        if (!isEmpty(user.getFirstName())) {
            assert user.getFirstName().equals(map.get(userKeys.firstName())) : "first names don't match";
        }
        if (!isEmpty(user.getLastName())) {
            assert user.getLastName().equals(map.get(userKeys.lastName())) : "last names don't match";
        }
        // Disabling this. The issue is that internally, updating the user has several saves written in to it as it is archived.
        // These update the serial string automatically and intercepting these would take a low-level re-write.
        if (checkSerialString) {
            assert user.getSerialString().equals(map.get(userKeys.serialString())) : "serial strings don't match. Expected " + user.getSerialString() + ", and got " + map.get(userKeys.serialString());
        }
        assert DateUtils.compareDates(user.getCreationTS(), map.getDate(userKeys.creationTS())) : "creation times don't match. Expected " + user.getCreationTS() + ". Got " + map.get(userKeys.creationTS());

    }

    protected boolean isEmpty(String x) {
        return x == null || x.isEmpty();
    }

    protected void checkUserAgainstMap(XMLMap map, User user) {
        checkUserAgainstMap(map, user, true);
    }

    /**
     * Check the status in the map (the {@link #getDBSClient()} call always contains the status)
     * and return if the requested status is there. See also
     * <ul>
     *     <li>{@link #getStatusKey(Map)} -- get the status key</li>
     *     <li>{@link #responseOk(Map)} -- check that the response was "ok"</li>
     *
     * </ul>
     *
     * @param m
     * @param value
     * @return
     */
    protected boolean checkStatusKey(Map m, long value) {
        if (m.containsKey(STATUS_KEY)) {
            return m.get(STATUS_KEY).equals(value);
        }
        return false;
    }

    /**
     * returns the values of the status key or -1 if there is no status.
     * @param m
     * @return
     */
    protected long getStatusKey(Map m) {
        if (m.containsKey(STATUS_KEY)) {
            return (long) m.get(STATUS_KEY);
        }
        return -1;
    }

    /**
     * Only checks that the status response was "ok". 
     * @param m
     * @return
     */
    protected boolean responseOk(Map m) {
        return checkStatusKey(m, STATUS_OK);
    }

    /**
     * Part of testing is to look directly into the store as well as check what goes over the wire. This
     * provider therefore should point to the local testing store.
     *
     * @return
     */
    public static CILTestStoreProviderI2 getTSProvider() {
        return (CILTestStoreProviderI2) ServiceTestUtils.getMySQLStoreProvider();
    }

    TwoFactorStore get2FStore() throws Exception {
        return getTSProvider().getTwoFactorStore();
    }

    static UserStore getUserStore() throws Exception {
        return getTSProvider().getUserStore();
    }

    ClientStore getClientStore() throws Exception {
        return getTSProvider().getClientStore();
    }

    ArchivedUserStore getArchivedUserStore() throws Exception {
        return getTSProvider().getArchivedUserStore();
    }

    TransactionStore getTransactionStore() throws Exception {
        return getTSProvider().getTransactionStore();
    }

    static IdentityProviderStore getIDPStore() throws Exception {
        return getTSProvider().getIDP();
    }

    public static User newUser() throws Exception {
        String x = getRandomString();
        return newUser("Muhammed-" + x, "Chang-" + x);
    }

    /**
     * Create a new UserMultiID that only has a remote user in it. This is exactly our legacy tests.
     *
     * @param x
     * @return
     */
    public static UserMultiID createRU(String x) {
        return new UserMultiID(new RemoteUserName(x));
    }

    public static UserMultiID createUMK() {
        return createUMK(getRandomString());
    }

    /**
     * This will create a new complete UserMultiID using all parameters, just appending the string
     * to eppn:, eptid: etc. to get valid uris. You should not be able to specify all of these. This is
     * just basically a list of all possible ones for testing and each test takes what it needs,
     * e.g. search by eppn.
     *
     * @param x
     * @return
     */
    public static UserMultiID createUMK(String x) {
        return new UserMultiID(new RemoteUserName("remoteUser:" + x),
                new EduPersonPrincipleName("eppn:" + x),
                new EduPersonTargetedID("eptid:" + x),
                new OpenID("openid:" + x),
                new OpenIDConnect("oidc:" + x),
                new PairwiseID("pairwise-id:" + x),
                new SubjectID("subject-id:" + x));
    }

    protected static User newUser(String firstName, String lastName) throws Exception {
        String rString = getRandomString();
        IdentityProvider idp = new IdentityProvider(BasicIdentifier.newID("urn:identity/prov/" + rString));
        getIDPStore().add(idp);
        User bob = getUserStore().createAndRegisterUser(createRU("remote-" + rString),
                idp.getIdentifier().toString(), "idp display name-" + rString,
                firstName,
                lastName,
                firstName.toLowerCase() + "." + lastName.toLowerCase() + "@" + rString + ".edu",
                "affiliation-" + rString,
                firstName + " " + lastName,
                "organization-" + rString);
        return bob;
    }
}
