package test.cilogon;

import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.storage.XMLMap;
import org.cilogon.oauth2.servlet.StatusCodes;
import org.cilogon.oauth2.servlet.storage.idp.IdentityProvider;
import org.cilogon.oauth2.servlet.storage.twofactor.TwoFactorInfo;
import org.cilogon.oauth2.servlet.storage.user.*;
import org.cilogon.oauth2.servlet.util.DBServiceException;
import org.cilogon.oauth2.servlet.util.DBServiceSerializer;
import org.junit.Test;
import org.oa4mp.delegation.common.storage.clients.Client;
import org.oa4mp.delegation.common.token.impl.AuthorizationGrantImpl;
import org.oa4mp.delegation.server.ServiceTransaction;
import org.oa4mp.server.api.ServiceConstantKeys;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static edu.uiuc.ncsa.security.core.util.BasicIdentifier.newID;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/30/14 at  1:26 PM
 */
public class DBServiceTests extends RemoteDBServiceTest {
    /**
     * This test shows that the API for getting an archived user works.
     *
     * @throws Exception
     */
    @Test
    public void testArchivedUser() throws Exception {
        Map m = null;
        try {
            m = getDBSClient().getLastArchivedUser(BasicIdentifier.newID("fake:user:id:123"));
        } catch (DBServiceException x) {
            assert x.checkMessage(StatusCodes.STATUS_USER_NOT_FOUND_ERROR);
        }

        User user = newUser();
        try {
            m = getDBSClient().getLastArchivedUser(user.getIdentifier());
        } catch (DBServiceException x) {
            assert x.checkMessage(StatusCodes.STATUS_USER_NOT_FOUND_ERROR);
        }

        // get the user for comparison
        XMLMap userMap = getDBSClient().getUser(user.getIdentifier());

        // Directly work with store and see it is propagated correctly.
        user.setFirstName("Aethelred");
        user.setLastName("Cerdicing");
        getArchivedUserStore().archiveUser(user.getIdentifier());
        getUserStore().update(user);
        // At this point, "user2" is what is in the archive for this identifier. "user" has different information in it.
        checkUserAgainstMap(userMap, getArchivedUserStore().getLastArchivedUser(user.getIdentifier()).getUser());
        // now there should be a user in the archived user store for this id.
        Map<String, Object> map3 = getDBSClient().getLastArchivedUser(user.getIdentifier());
        for (String k : map3.keySet()) {
            assert map3.get(k).equals(userMap.get(k)) : "user and archived user failed to match for key \"" + k + "\"";
        }
    }

    /**
     * Since CILogon 2.0, there are several new fields for a user. This test will change any of several items
     * and check that the contract for detecting changes has not been violated.
     *  <h1>Updated 2020-03-24T15:45:49.545Z</h1>
     *  This test is now disabled since we are aiming to remove archiving users from the database. This means that
     *  there will be no further testing of these components and if they break because of code changes it will
     *  be silent.
     * @throws Exception
     */
    //@Test
    public void OLDTEST() throws Exception {
    //public void testArchiverUserTrigger() throws Exception {
        User user = newUser();   // This creates the user and stores it.
        // First off, the items that must trigger a user archive. idp display name, first name, last name, email
        User newUser = user.clone();
        try {
            getDBSClient().getLastArchivedUser(user.getIdentifier());
            assert false : "Was able to get an archived user for a completely new user.";
        } catch (Throwable t) {
            // Should be nothing here to start with.

            assert true;
        }

        // Changing the first name should trigger an archive user event (AUE)
        newUser.setFirstName("Blarfo");
        assert testAUChange(newUser, true) : "Changing first name did not archive user.";

        // And for the last name
        newUser.setLastName("Falafel");
        assert testAUChange(newUser, true) : "Changing last name did not archived user.";
        // Changing the email should trigger an AUE.
        newUser.setEmail("fnord@fnu.edu");
        assert testAUChange(newUser, true) : "Changing email did not archived user.";

        // Changing the IDP display name should trigger an AUE
        newUser.setIDPName("totally groovy idp name");
        assert testAUChange(newUser, true) : "Changing IDP display name did not archived user.";
        
        // Changing Display should trigger a new AUE
        newUser.setDisplayName("Blarfo Q. Falafel IV");
        assert testAUChange(newUser, true) : "Changing display name  did not archived user.";

        // The follow changes should NOT cause an AUE

        newUser.setAffiliation("test affiliation");
        assert testAUChange(newUser, false) : "Affiliation change caused archived user event.";

        newUser.setOrganizationalUnit("test organization");
        assert testAUChange(newUser, false) : "Display name change caused archived user event.";

        newUser.setePPN(new EduPersonPrincipleName("eppn:test/1"));
        assert testAUChange(newUser, false) : "EPPN change caused archived user event.";
        // NOTE have to null out the EPPN, EPTID etc. after use since one failure mode on the server
        // is to reject any request with all of these set, since that is impossible in practice and
        // would probably represent a serious internal consistency issue.

        newUser.setePPN(null);

        newUser.setePTID(new EduPersonTargetedID("eptid:test/1"));
        assert testAUChange(newUser, false) : "EPTID change caused archived user event.";
        newUser.setePTID(null);

        newUser.setOpenID(new OpenID("openid:test/1"));
        assert testAUChange(newUser, false) : "Open ID change caused archived user event.";
        newUser.setOpenID(null);

        newUser.setOpenIDConnect(new OpenIDConnect("openid:connect/test/1"));
        assert testAUChange(newUser, false) : "OpenID Connect change caused archived user event.";
        getUserStore().remove(user.getIdentifier());
    }

    /**
     * This will take a user that is supposed to change (the flag tells what to check for
     * returned values are if isChanged == true, true if changed, false otherwise.
     * if isChanged == false, true if UNCHANGED, false if otherwise.<br/>
     * In otherwords, this returns true if the expected behavior was observed.
     *
     * @param user
     * @param isChanged
     * @return
     * @throws Exception
     */
    protected boolean testAUChange(User user, boolean isChanged) throws Exception {
        long pre = getArchivedUserStore().getAllByUserId(user.getIdentifier()).size();
        getDBSClient().getUser(user);
        long post = getArchivedUserStore().getAllByUserId(user.getIdentifier()).size();
        if (isChanged) {
            return post == pre + 1;
        }
        return pre == post;
    }

    @Test
    public void testPortalParameter() throws Exception {

        Client client = (Client) getClientStore().create();
        ServiceTransaction t = (ServiceTransaction) getTransactionStore().create();
        // Make sure the authorization grant is not something random.
        AuthorizationGrantImpl ag = new AuthorizationGrantImpl(t.getIdentifier().getUri());
        t.setAuthorizationGrant(ag);

        //t.setAuthorizationGrant(getTSProvider().getSE().getTokenForge().getAuthorizationGrant());
        t.setCallback(createToken("callbackURI"));
        client.setName("Test AbstractDBService portal name");
        client.setHomeUri(createToken("homeURI").toString());
        client.setErrorUri(createToken("errorURI").toString());
        getClientStore().save(client);
        t.setClient(client);

        getTransactionStore().save(t);
        //now its in the store. We have to get it.
        Map<String, Object> t2; // we can't actually recreate the transaction completely -- that is not the aim. So we use a hash map instead.
        String tokenKey = (String) getTSProvider().getConfigLoader().getConstants().get(ServiceConstantKeys.TOKEN_KEY);
        t2 = getDBSClient().getPortalParameters(t.getAuthorizationGrant().getToken());
        assert t2.get(DBServiceSerializer.CILOGON_CALLBACK_URI).equals(t.getCallback().toString());
        assert t2.get(DBServiceSerializer.CILOGON_FAILURE_URI).equals(client.getErrorUri().toString());
        assert t2.get(DBServiceSerializer.CILOGON_SUCCESS_URI).equals(client.getHomeUri().toString());
        assert t2.get(tokenKey).equals(t.getAuthorizationGrant().getToken());
        assert t2.get(DBServiceSerializer.CILOGON_PORTAL_NAME).equals(client.getName());
        // now for the error modes
        try {
            getDBSClient().getPortalParameters("foo");
            //parseTransaction(doGet(AbstractDBService.GET_PORTAL_PARAMETER, new String[][]{{tokenKey, "foo"}}));
            assert false : "bad identifier should result in no transaction being found";
        } catch (DBServiceException x) {
            assert x.checkMessage(StatusCodes.STATUS_TRANSACTION_NOT_FOUND);
        }
        try {
            getDBSClient().getPortalParameters("");
            //parseTransaction(doGet(AbstractDBService.GET_PORTAL_PARAMETER, new String[][]{{}}));
            assert false : "Missing parameter";
        } catch (DBServiceException x) {
            assert x.checkMessage(StatusCodes.STATUS_TRANSACTION_NOT_FOUND);
        }
    }


    @Test
    public void testIDPs() throws Exception {
        List<IdentityProvider> originalIdps = getDBSClient().getAllIdps();

        // ok, so we add a couple to this list, save it, read it back and compare.
        originalIdps.add(new IdentityProvider(newID(createToken("idp"))));
        originalIdps.add(new IdentityProvider(newID(createToken("idp"))));
        originalIdps.add(new IdentityProvider(newID(createToken("idp"))));
        getDBSClient().addIdps(originalIdps);
        // Test that adding idps to the list preserves the original set.
        List<IdentityProvider> newIdps = getDBSClient().getAllIdps();
        // ok, now compare
        assert originalIdps.size() == newIdps.size();
        for (IdentityProvider idp : originalIdps) {
            assert newIdps.contains(idp);
        }

        // Check that a complete replace of every idp does the right thing.
        newIdps = new ArrayList<IdentityProvider>();
        for (int i = 0; i < count; i++) {
            newIdps.add(new IdentityProvider(newID(createToken("idp"))));
        }
        getDBSClient().addIdps(newIdps);
        List<IdentityProvider> newIdps2 = getDBSClient().getAllIdps();
        assert (newIdps.size() + originalIdps.size()) == newIdps2.size() : "Expected " + (newIdps.size() + originalIdps.size()) + " and got " + newIdps2.size();
        for (IdentityProvider idp : newIdps) {
            assert newIdps2.contains(idp);
        }

        // Finally, check that saving an empty list fails in the right way.
        try {
            getDBSClient().addIdps(new ArrayList<IdentityProvider>());
        } catch (DBServiceException x) {
            assert x.checkMessage(StatusCodes.STATUS_IDP_SAVE_FAILED);
        }
    }


    @Test
    public void testIDP_ListMissingOne() throws Exception {
        List<IdentityProvider> originalIdps = getDBSClient().getAllIdps();

        List<IdentityProvider> reducedIDPList = new LinkedList<>(originalIdps);
        if(reducedIDPList.size() == 0){
            System.out.println("Warning: no IDPs found on this system. Test aborted.");
            // case of a completely new install
            return;
        }
        IdentityProvider removedIDP = reducedIDPList.remove(0);

        XMLMap map = getDBSClient().addIdps(reducedIDPList);

        // Test that adding idps to the list preserves the original set.
        List<IdentityProvider> newIdps = getDBSClient().getAllIdps();
        assert newIdps.size() == originalIdps.size() : "Testing of IDP list with one removed failed. Expected " + originalIdps.size() + " elements and got " + newIdps.size();
        assert newIdps.contains(removedIDP);
    }

    @Test
    public void testGet2FInfo() throws Exception {
        String info = getRandomString(256);
        User user = newUser();
        TwoFactorInfo tfi = new TwoFactorInfo(user.getIdentifier(), info);
        get2FStore().save(tfi);
        // so it's in the store, let's re-get it and see if it's the same
        TwoFactorInfo tfi2 = getDBSClient().getTwoFactorInfo(user.getIdentifier());
        assert tfi2.getIdentifier().equals(tfi.getIdentifier()) : "Identifiers don't match";
        assert tfi2.getInfo().equals(tfi.getInfo()) : "Info doesn't match";
    }

    @Test
    public void testSet2FInfo() throws Exception {
        String info = getRandomString(256);
        User user = newUser();
        TwoFactorInfo tfi = new TwoFactorInfo(user.getIdentifier(), info);
        //   get2FStore().save(tfi);
        // so it's in the store, let's re-get it and see if it's the same
        getDBSClient().setTwoFactorInfo(tfi);
        TwoFactorInfo tfi2 = get2FStore().get(tfi.getIdentifier());
        assert tfi2.getIdentifier().equals(tfi.getIdentifier()) : "Identifiers don't match";
        assert tfi2.getInfo().equals(tfi.getInfo()) : "Info doesn't match";
    }
}
