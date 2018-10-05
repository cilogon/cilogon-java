package org.cilogon.d2;

import edu.uiuc.ncsa.myproxy.oa4mp.server.ServiceConstantKeys;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.delegation.server.ServiceTransaction;
import edu.uiuc.ncsa.security.delegation.storage.Client;
import edu.uiuc.ncsa.security.storage.XMLMap;
import org.cilogon.d2.servlet.AbstractDBService;
import org.cilogon.d2.storage.*;
import org.cilogon.d2.twofactor.TwoFactorInfo;
import org.cilogon.d2.util.*;
import org.junit.Test;

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
            assert x.checkMessage(AbstractDBService.STATUS_USER_NOT_FOUND_ERROR);
        }

        User user = newUser();
        try {
            m = getDBSClient().getLastArchivedUser(user.getIdentifier());
        } catch (DBServiceException x) {
            assert x.checkMessage(AbstractDBService.STATUS_USER_NOT_FOUND_ERROR);
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
     *
     * @throws Exception
     */
    @Test
    public void testArchiverUserTrigger() throws Exception {
        User user = newUser();
        // First off, the items that must trigger a user archive. idp display name, first name, last name, email
        User newUser = user.clone();
        XMLMap auMap  = null;
        XMLMap uMap  = null;
        UserKeys userKeys = new UserKeys();
        try {
            auMap = getDBSClient().getLastArchivedUser(user.getIdentifier());
            assert false : "Was able to get an archived user for a completely new user.";
        } catch (Throwable t) {
                    // Should be nothing here to start with.

            assert true;
        }
        long pre = getArchivedUserStore().getAllByUserId(user.getIdentifier()).size();

        // Changing the first name should trigger an user archive event (AUE)
        newUser.setFirstName("Blarfo");
        assert testAUChange(newUser, true): "First names is incorrect for the archived user.";

        // And for the last name
        newUser.setLastName("Falafel");
        assert testAUChange(newUser, true) : "Last name is incorrect for the archived user.";
        // Changing the email should trigger an AUE.
        newUser.setEmail("fnord@fnu.edu");
        assert testAUChange(newUser, true): "Email is incorrect for the archived user.";

        // Changing the IDP display name should trigger an AUE
        newUser.setIDPName("totally groovy idp name");
        assert testAUChange(newUser, true) : "IDP display name incorrect for the archived user.";
        // The follow changes should NOT cause an AUE
        // display name (different from IDP display name)
        newUser.setDisplayName("blarfo falafel");
        assert testAUChange(newUser, false) : "Display name change caused archived user event.";

        newUser.setAffiliation("test affiliation");
        assert testAUChange(newUser, false) : "Affiliation change caused archived user event.";

        newUser.setOrganizationalUnit("test organization");
        assert testAUChange(newUser, false) : "Display name change caused archived user event.";

        newUser.setePPN(new EduPersonPrincipleName("eppn:test/1"));
         assert testAUChange(newUser, false): "EPPN change caused archived user event.";
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

    protected boolean testAUChange(User user, boolean isChanged) throws Exception {
        long pre = getArchivedUserStore().getAllByUserId(user.getIdentifier()).size();
        getDBSClient().getUser(user);
        long post = getArchivedUserStore().getAllByUserId(user.getIdentifier()).size();
        if(isChanged){
            return post  == pre + 1;
        }
        return pre == post;
    }
    @Test
    public void testPortalParameter() throws Exception {

        Client client = (Client) getClientStore().create();
        ServiceTransaction t = (ServiceTransaction) getTransactionStore().create();
        t.setAuthorizationGrant(getTSProvider().getSE().getTokenForge().getAuthorizationGrant());
        t.setCallback(createToken("callbackURI"));
        client.setName("Test AbstractDBService portal name");
        client.setHomeUri(createToken("homeURI").toString());
        client.setErrorUri(createToken("errorURI").toString());
        getClientStore().save(client);
        t.setClient(client);
        getTransactionStore().save(t);
        //now its in the store. We have to get it.
        Map<String, Object> t2; // we can't actually recreate the transaction completely -- that is not the aim. So we use a hash map instead.
        String tokenKey = (String)getTSProvider().getConfigLoader().getConstants().get(ServiceConstantKeys.TOKEN_KEY);
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
            assert x.checkMessage(AbstractDBService.STATUS_TRANSACTION_NOT_FOUND);
        }
        try {
            getDBSClient().getPortalParameters("");
            //parseTransaction(doGet(AbstractDBService.GET_PORTAL_PARAMETER, new String[][]{{}}));
            assert false : "Missing parameter";
        } catch (DBServiceException x) {
            assert x.checkMessage(AbstractDBService.STATUS_TRANSACTION_NOT_FOUND);
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
            assert x.checkMessage(AbstractDBService.STATUS_IDP_SAVE_FAILED);
        }
    }


    @Test
    public void testIDP_ListMissingOne() throws Exception {
        List<IdentityProvider> originalIdps = getDBSClient().getAllIdps();

        List<IdentityProvider> reducedIDPList = new LinkedList<>(originalIdps);
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
