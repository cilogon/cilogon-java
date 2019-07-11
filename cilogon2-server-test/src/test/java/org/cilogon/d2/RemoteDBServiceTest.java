package org.cilogon.d2;

import edu.uiuc.ncsa.myproxy.oa4mp.server.ServiceConstantKeys;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.core.util.DateUtils;
import edu.uiuc.ncsa.security.delegation.server.storage.ClientStore;
import edu.uiuc.ncsa.security.delegation.storage.TransactionStore;
import edu.uiuc.ncsa.security.storage.XMLMap;
import edu.uiuc.ncsa.security.util.TestBase;
import org.cilogon.d2.storage.*;
import org.cilogon.d2.twofactor.TwoFactorStore;
import org.cilogon.d2.util.ArchivedUserStore;
import org.cilogon.d2.util.DBServiceClient;
import org.cilogon.d2.util.UserKeys;

import java.util.Map;

import static org.cilogon.d2.servlet.AbstractDBService.STATUS_KEY;
import static org.cilogon.d2.servlet.AbstractDBService.STATUS_OK;

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

    public static String host = "http://localhost:44444/oauth/dbService";
    DBServiceClient dbsClient = null;

    public DBServiceClient getDBSClient() {
        if (dbsClient == null) {
            dbsClient = new DBServiceClient(getHost(), (String)getTSProvider().getConfigLoader().getConstants().get(ServiceConstantKeys.TOKEN_KEY));
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

        assert user.getIdP().equals(map.get(userKeys.idp())) : "IDP's don't match";
        assert user.getIDPName().equals(map.get(userKeys.idpDisplayName())) : "IDP display names don't match. Expected " + user.getIDPName() + " and got " + (map.get(userKeys.idpDisplayName()));
        assert user.getFirstName().equals(map.get(userKeys.firstName())) : "first names don't match";
        assert user.getLastName().equals(map.get(userKeys.lastName())) : "last names don't match";
        if (checkSerialString) {
            assert user.getSerialString().equals(map.get(userKeys.serialString())) : "serial strings don't match. Expected " + user.getSerialString() + ", and got " + map.get(userKeys.serialString());
        }
        assert DateUtils.compareDates(user.getCreationTime(), map.getDate(userKeys.creationTimestamp())) : "creation times don't match. Expected " + user.getCreationTime() + ". Got " + map.get(userKeys.creationTimestamp());

    }

    void checkUserAgainstMap(XMLMap map, User user) {
        checkUserAgainstMap(map, user, true);
    }

    boolean checkStatusKey(Map m, long value) {
        if(m.containsKey(STATUS_KEY)){
            return m.get(STATUS_KEY).equals(value);

        }
        return false;
    }

    boolean responseOk(Map m) {
        return checkStatusKey(m, STATUS_OK);
    }

    /**
     * Part of testing is to look directly into the store as well as check what goes over the wire. This
     * provider therefore should point to the local testing store.
     *
     * @return
     */
    public static CILTestStoreProviderI2 getTSProvider() {
      //  return (CILTestStoreProvider) ServiceTestUtils.getPgStoreProvider();
        return (CILTestStoreProviderI2) ServiceTestUtils.getMySQLStoreProvider();
        //return (CILTestStoreProvider) ServiceTestUtils.getMySQLStoreProvider();
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
     * Create a new UserMultiKey that only has a remote user in it. This is exactly our legacy tests.
     *
     * @param x
     * @return
     */
    public static UserMultiKey createRU(String x) {
        return new UserMultiKey(new RemoteUserName(x));
    }

    public static UserMultiKey createUMK() {
        return createUMK(getRandomString());
    }

    /**
     * This will create a new complete UserMultiKey using all parameters, just appending the string
     * to eppn:, eptid: etc. to get valid uris
     *
     * @param x
     * @return
     */
    public static UserMultiKey createUMK(String x) {
        return new UserMultiKey(new RemoteUserName("remoteUser:" + x),
                new EduPersonPrincipleName("eppn:" + x),
                new EduPersonTargetedID("eptid:" + x),
                new OpenID("openid:" + x),
                new OpenIDConnect("oidc:" + x));
    }

    protected static User newUser(String firstName, String lastName) throws Exception {
        String rString = getRandomString();
        IdentityProvider idp = new IdentityProvider(BasicIdentifier.newID("urn:identity/prov/" + rString));
        getIDPStore().add(idp);
        User bob = getUserStore().createAndRegisterUser(createRU("remote-" + rString),
                idp.getIdentifier().toString(), "idp display name",
                firstName,
                lastName,
                firstName.toLowerCase() + "." + lastName.toLowerCase() + "@" + rString + ".edu",
                "affiliation" + rString,
                firstName + " " + lastName,
                "organization" + rString);
        return bob;
    }
}
