package test.cilogon;

import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.core.util.BeanUtils;
import edu.uiuc.ncsa.security.storage.XMLMap;
import org.cilogon.oauth2.servlet.StatusCodes;
import org.cilogon.oauth2.servlet.storage.twofactor.TwoFactorInfo;
import org.cilogon.oauth2.servlet.storage.twofactor.TwoFactorSerializationKeys;
import org.cilogon.oauth2.servlet.storage.user.User;
import org.cilogon.oauth2.servlet.storage.user.UserMultiID;
import org.cilogon.oauth2.servlet.util.DBServiceException;
import org.junit.Test;

import java.util.Map;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/30/14 at  1:25 PM
 */
public class DBServiceUserTests extends RemoteDBServiceTest {

    /*
      Regression test for CIL-11: Multiply fetching a user should return ok, not user changed.
    */
    @Test
    public void testCIL11() throws Exception {
        testCIL11(true);
        testCIL11(false);
    }

    protected void testCIL11(boolean useIDP) throws Exception {
        User user = newUser(useIDP);

        for (int i = 0; i < count; i++) {
            assert responseOk(getDBSClient().getUser(user.getIdentifier()));
        }
        String rString = getRandomString();
        user.setEmail("name" + rString + "@foo.bar");
        user.setFirstName("firstName-" + rString);
        user.setLastName("lastName-" + rString);
        if (useIDP) {
            user.setIDPName("idpName-" + rString);
        }
        getArchivedUserStore().archiveUser(user.getIdentifier());
        getUserStore().update(user);
        for (int i = 0; i < count; i++) {
            assert responseOk(getDBSClient().getUser(user.getUserMultiKey(), user.getIdP()));
        }
    }

    @Test
    public void testCreateUser() throws Exception {
        XMLMap m = getDBSClient().createUser(createRU("test+remote+user+(" + getRandomString() + ")"), "test//idp++" + getRandomString(),
                "1ÐP Ð1$p£4¥ |\\|4|\\/|3", "firstName", "lastName", "robespierre@guillotine.org", "affiliation" + getRandomString(),
                "display name" + getRandomString(), "organization" + getRandomString());
        User user2 = getUserStore().get(BasicIdentifier.newID(m.get(userKeys.identifier()).toString()));
        checkUserAgainstMap(m, user2);
    }

    /**
     * Test that we can create a new user by a call to the DB service with a <i>minimal</i>   call,
     * i.e., remote user and the idp. This means no other information for the user is set.
     *
     * @throws Exception
     */
    @Test
    public void testGetNewUser() throws Exception {
        testGetNewUser(true);
        testGetNewUser(false);
    }

    protected void testGetNewUser(boolean useIDP) throws Exception {
        UserMultiID umk = createRU("remote-user-" + getRandomString());
        String idp = null;
        if (useIDP) {
            idp = "test-idp-" + getRandomString();
        }
        // create a new user.
        XMLMap map = getDBSClient().getUser(umk, idp);
        assert getDBSClient().hasUser(umk, idp);
        Identifier uid = BasicIdentifier.newID(map.get(userKeys.identifier()).toString());
        User user2 = getUserStore().get(uid);
        checkUserAgainstMap(map, user2);
    }

    @Test
    public void testUpdateUser() throws Exception {
        testUpdateUser(true);
        testUpdateUser(false);
    }

    protected void testUpdateUser(boolean useIDP) throws Exception {
        User user = newUser(useIDP);
        // test is to send new information for a user.
        String firstName = "Дмитрий";
        String lastName = "Шостакович+源";  // Russian - Japanese last name...
        String email = "guido@medici.com";
        String affiliation = "affilation" + lastName;
        String displayName = firstName + " " + lastName;
        String organizationalUnit = "organization " + lastName;
        Map<String, Object> userMap = getDBSClient().updateUser(user.getUserMultiKey(),
                user.getIdP(),
                user.getIDPName(),
                firstName,
                lastName,
                email,
                affiliation,
                displayName,
                organizationalUnit);
        assert checkStatusKey(userMap, StatusCodes.STATUS_USER_SERIAL_STRING_UPDATED);
        // The user has been updated. We should have the updated user's information returned to us.
        // NOTE if you are getting screwy characters in this test back from the server it is probably because you
        // need to add the attribute of
        //
        //  URIEncoding="UTF-8"
        //
        // to your connectors in the tomcat server.xml file. Tomcat unpacks this differently on the server if this is not
        // added and the result will be that the system cannot recognize international characters.
        assert userMap.get(userKeys.identifier()).equals(user.getIdentifier().toString());
        assert BeanUtils.checkBasic(userMap.get(userKeys.remoteUser()), user.getRemoteUser().getName());
        if (useIDP) {
            assert userMap.get(userKeys.idp()).equals(user.getIdP());
            assert userMap.get(userKeys.idpDisplayName()).equals(user.getIDPName());
        }
        assert userMap.get(userKeys.firstName()).equals(firstName) : "First names are different. Have \"" + firstName + "\", got \"" + userMap.get(userKeys.firstName()) + "\".";
        assert userMap.get(userKeys.lastName()).equals(lastName) : "Last names are different. Have \"" + lastName + "\", got \"" + userMap.get(userKeys.lastName()) + "\".";
        assert userMap.get(userKeys.email()).equals(email);
        assert userMap.get(userKeys.affiliation()).equals(affiliation) : " Affiliations do not match";
        assert userMap.get(userKeys.displayName()).equals(displayName) : "Display names do not match";
        assert userMap.get(userKeys.organizationalUnit()).equals(organizationalUnit) : " org units do not match";


        // now check that the last archived user is the same as the very first user
        XMLMap map = getDBSClient().getLastArchivedUser(user.getIdentifier());
        checkUserAgainstMap(map, user);
    }

    @Test
    public void testHasUser() throws Exception {
        testHasUser(true);
        testHasUser(false);
    }
    protected void testHasUser(boolean useIDP) throws Exception {
        // start with two bad cases
        assert !getDBSClient().hasUser(BasicIdentifier.newID("foo:bar:baz:"));
        assert !getDBSClient().hasUser(createRU("foo"), "bar");

        User user = newUser(useIDP);
        assert getDBSClient().hasUser(user.getIdentifier());
        assert getDBSClient().hasUser(user.getUserMultiKey(), user.getIdP());
    }

    /**
     * Something of a regression test to be sure that characters that are correctly supplied are
     * processed right. This roundtrips a bunch of special ascii characters and checks they are the
     * same when returned. NOTE: the actual encoding type, UTF-8 must be set in the tomcat connector
     * for the web container. This is the only way to get reliable behavior across different versions,
     * JVMs &c., &c.
     *
     * @throws Exception
     */
    @Test
    public void testGetNewUserSpecialChars() throws Exception {
        testGetNewUserSpecialChars(true);
        testGetNewUserSpecialChars(false);
    }
    protected void testGetNewUserSpecialChars(boolean useIDP) throws Exception {
        String badChars = "~!@#$%^&*(//\\)_" + getRandomString();
        String firstname = "first+name" + badChars;
        String lastname = "last+name" + badChars;
        String email = "email@domain+name://///" + getRandomString();
        UserMultiID umk = createRU("remote+user" + badChars);
        String idp = null;
        String idpName = null;
        if(useIDP) {
             idp = "test+idp+" + getRandomString();
             idpName = "test//idp/" + getRandomString();
        }
        String affiliation = "affiliation" + badChars;
        String displayName = firstname + " " + lastname;
        String organizationalUnit = "organization" + badChars;

        // create a new user. The escaped characters are sent and should be returned unescaped in the map.
        XMLMap map = getDBSClient().getUser(umk, idp, idpName, firstname, lastname, email, affiliation,
                displayName, organizationalUnit);
        assert getDBSClient().hasUser(umk, idp);
        Identifier uid = BasicIdentifier.newID(map.get(userKeys.identifier()).toString());
        User user2 = getUserStore().get(uid);
        checkUserAgainstMap(map, user2);
    }

    @Test
    public void testGetUser() throws Exception {
        testGetUser(true);
        testGetUser(false);
    }
    protected void testGetUser(boolean useIDP) throws Exception {
        User user = newUser(useIDP);
        XMLMap m = getDBSClient().getUser(user.getIdentifier());
        checkUserAgainstMap(m, user);

        // API dictates that any call for the user that does not just use the id requires all  parameters
        m = getDBSClient().getUser(user);
        checkUserAgainstMap(m, user, true);
        String idp = null;
        String idpName = null;
        if(useIDP){
            idp =user.getIdP();
            idpName = user.getIDPName() + "foo1";
        }
        // Next test gets a user via the servlet and checks it was updated in the store
        m = getDBSClient().getUser(user.getUserMultiKey(),
                idp,
                idpName,
                user.getFirstName() + "foo2",
                user.getLastName() + "foo3",
                user.getEmail() + "foo4",
                user.getAffiliation() + "foo",
                user.getDisplayName() + "foo",
                user.getOrganizationalUnit() + "foo");
        user = getUserStore().get(user.getIdentifier());
        checkUserAgainstMap(m, user);
    }

    /**
     * Next we set the user info AND some two factor info and check that it all comes back.
     *
     * @throws Exception
     */
    @Test
    public void testGetUser2F() throws Exception {
        testGetUser2F(true);
        testGetUser2F(false);
    }
    protected void testGetUser2F(boolean useIDP) throws Exception {
        User user = newUser(useIDP);
        TwoFactorInfo tfi = new TwoFactorInfo(user.getIdentifier(), getRandomString(256));
        get2FStore().save(tfi);
        XMLMap m = getDBSClient().getUser(user.getIdentifier());
        checkUserAgainstMap(m, user);
        TwoFactorSerializationKeys t2k = new TwoFactorSerializationKeys();
        assert m.get(t2k.info()).toString().equals(tfi.getInfo());

        // API dictates that any call for the user that does not just use the id requires all 6 parameters
        m = getDBSClient().getUser(user);
        assert m.containsKey(t2k.info()) : "Getting user with remote-user that has two factor does not return two factor information.";
        checkUserAgainstMap(m, user, true);
        assert m.get(t2k.info()).toString().equals(tfi.getInfo());
        String idp =null;
        String idpName = null;
        if(useIDP){
            idp = user.getIdP();
            idpName = user.getIDPName() + "foo1";
        }
        // Next test gets a user via the servlet and checks it was updated in the store
        m = getDBSClient().getUser(user.getUserMultiKey(), idp,
                idpName,
                user.getFirstName() + "foo2",
                user.getLastName() + "foo3",
                user.getEmail() + "foo4",
                user.getAffiliation() + "foo",
                user.getDisplayName() + "foo",
                user.getOrganizationalUnit() + "foo");
        user = getUserStore().get(user.getIdentifier());
        checkUserAgainstMap(m, user);
        assert m.get(t2k.info()).toString().equals(tfi.getInfo());

    }

    @Test
    public void testRemoveUser() throws Exception {
        testRemoveUser(true);
        testRemoveUser(false);
    }
    protected void testRemoveUser(boolean useIDP) throws Exception {
        User user = newUser(useIDP);
        assert getDBSClient().removeUser(user.getIdentifier());
        try {
            // now it should fail ,since we just removed this user:
            getDBSClient().removeUser(user.getIdentifier());
        } catch (DBServiceException x) {
            x.checkMessage(StatusCodes.STATUS_USER_NOT_FOUND_ERROR);
        }

        // check in another way that it is gone
        assert !getDBSClient().hasUser(user.getIdentifier());

        // the user should be gone from the main database, but still archived.
        XMLMap map = getDBSClient().getLastArchivedUser(user.getIdentifier());
        checkUserAgainstMap(map, user);
        try {
            // now try to remove a dud user.
            getDBSClient().removeUser(BasicIdentifier.newID("fake:user:123"));
        } catch (DBServiceException x) {
            x.checkMessage(StatusCodes.STATUS_USER_NOT_FOUND_ERROR);
        }
    }

    @Test
    public void testGetUserID() throws Exception {
        testGetUserID(true);
        testGetUserID(false);
    }
    protected void testGetUserID(boolean useIDP) throws Exception {
        User user = newUser(useIDP);
        assert user.getIdentifier().equals(getDBSClient().getUserId(user.getUserMultiKey(), user.getIdP()));
    }

}
