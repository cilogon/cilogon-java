package test.cilogon.impl;

import edu.uiuc.ncsa.security.storage.XMLMap;
import org.cilogon.oauth2.servlet.StatusCodes;
import org.cilogon.oauth2.servlet.storage.user.DNState;
import org.cilogon.oauth2.servlet.storage.user.User;
import org.cilogon.oauth2.servlet.storage.user.UserStore;
import org.cilogon.oauth2.servlet.util.DBServiceException;
import org.cilogon.oauth2.servlet.util.DNUtil;
import org.cilogon.oauth2.servlet.util.UserConverter;
import org.cilogon.oauth2.servlet.util.UserKeys;
import org.junit.Test;
import test.cilogon.RemoteDBServiceTest;

import static org.cilogon.oauth2.servlet.servlet.AbstractDBService.GET_USER;
import static org.cilogon.oauth2.servlet.servlet.AbstractDBService.distinguishedNameField;


/**
 * A series of tests to show how DNs (distinguished names for certs) and serial strings
 * are now handled in the wake of CIL-540 (which relaxed many requirements and necessitated a
 * complete change in the logic of how it is all handled). Hard bits are that it all must be
 * 100% backwards compatible so that no existing user should have any of their certs changed
 * or updated needlessly.
 * <p>Created by Jeff Gaynor<br>
 * on 3/24/20 at  2:12 PM
 */
public class DNTests extends RemoteDBServiceTest {

    /**
     * Do a series of updates.
     * <ul>
     *     <li>Initial creation of user returns {@link StatusCodes#STATUS_NEW_USER}</li>
     *     <li>Subsequent updates of information return {@link StatusCodes#STATUS_OK}</li>
     *     <li>No changes to serial string</li>
     *     <li>Changes to insufficient information do not trigger changes to serial string</li>
     *     <li>When there is enough information, response is {@link StatusCodes#STATUS_USER_SERIAL_STRING_UPDATED}</li>
     * </ul>
     *
     * @throws Exception
     */
    @Test
    public void testIncrementalUpdate() throws Exception {
        String r = getRandomString();
        XMLMap userMap = new XMLMap();

        userMap.put(userKeys.eptid(), "https://idp.bigstate.edu/idp/shibboleth!" + r);
        userMap.put(userKeys.idp(), "https://idp.bigstate.edu/idp/shibboleth");
        UserConverter userConverter = new UserConverter(userKeys, null);
        // Here's how it works: Update various fields that do not allow the user to get a new serial string
        // and double check that the response is "new user" first time and "ok" after that.


        XMLMap rMap = getDBSClient().doGet(GET_USER, userMap);
        String oldSS = rMap.getString(userKeys.serialString());
        assert getStatusKey(rMap) == StatusCodes.STATUS_NEW_USER;
        assert !rMap.containsKey(distinguishedNameField) : unexpectedDNMessage;

        userMap.put(userKeys.email(), "bob" + r + "@bigstate.edu");
        rMap = checkNoChange(userMap, oldSS);

        // Double check that the method in user is behaving right.
        User user = new User(null, null);
        userConverter.fromMap(rMap, user);
        assert !user.canGetCert();

        userMap.put(userKeys.email(), "bob2" + r + "@bigstate.edu");
        checkNoChange(userMap, oldSS);


        // Now we add a first name then change it. At no point should the serial string increment.

        userMap.put(userKeys.firstName(), "Bob");
        checkNoChange(userMap, oldSS);

        userMap.put(userKeys.firstName(), "Robert");
        checkNoChange(userMap, oldSS);

        userMap.put(userKeys.idpDisplayName(), "Big State University");
        checkNoChange(userMap, oldSS);

        // Finally we add a last name, so it can get a serial string and DN.
        userMap.put(userKeys.lastName(), "Chang");
        rMap = checkUpdated(userMap, oldSS, false); // no SS update, just return what's there
        userConverter.fromMap(rMap, user);
        assert user.canGetCert();   // double check on user call.

        // cleanup
        getDBSClient().removeUser(rMap.getIdentifier(userKeys.userID()));

    }

    UserKeys userKeys = new UserKeys();

    /**
     * Assumes serial string is changed.
     *
     * @param userMap
     * @param oldSS
     * @return
     */
    private XMLMap checkUpdated(XMLMap userMap, String oldSS, boolean ssUpdate) {
        XMLMap rMap = getDBSClient().doGet(GET_USER, userMap);
        assert getStatusKey(rMap) == (ssUpdate ? StatusCodes.STATUS_USER_SERIAL_STRING_UPDATED : StatusCodes.STATUS_OK); // user updated = serial string
        if (ssUpdate) {
            assert !rMap.getString(userKeys.serialString()).equals(oldSS);
        } else {
            assert rMap.getString(userKeys.serialString()).equals(oldSS);
        }
        assert rMap.containsKey(distinguishedNameField) : missingDNMessage;
        return rMap;
    }

    private XMLMap checkNoChange(XMLMap userMap, String oldSS) {
        XMLMap rMap;
        rMap = getDBSClient().doGet(GET_USER, userMap);
        assert getStatusKey(rMap) == StatusCodes.STATUS_OK; // user updated = serial string
        assert rMap.getString(userKeys.serialString()).equals(oldSS);
        assert !rMap.containsKey(distinguishedNameField) : unexpectedDNMessage + ":" + rMap.getString(distinguishedNameField);
        return rMap;
    }

    String unexpectedDNMessage = "Response contains " + distinguishedNameField + " and it should not.";
    String missingDNMessage = "Missing " + distinguishedNameField + " in response";


    /**
     * (1) add new user with just oidc, status=2, no distinguished_name. good.
     * (2) add user again with same oidc and display_name, status=0, no distinguished_name,
     * serial_string unchanged. good.
     * (3) add user again with same oidc, display_name, and additional email, status=0,
     * distinguished_name returned. not good???
     * I think that since the distinguished_name went from not returned (empty) to returned (not empty),
     * status should be 4 (user changed). but maybe it's not that important.
     *
     * @throws Exception
     */
    @Test
    public void testNumber2() throws Exception {
        String r = getRandomString();
        XMLMap userMap = new XMLMap();
        String oidc = System.currentTimeMillis() + r;
        String firstName = "Mohammed";
        String lastName = "Chang";
        userMap.put(userKeys.oidc(), oidc);
        userMap.put(userKeys.idp(), "https://idp.google.com/idp/" + r);
        userMap.put(userKeys.idpDisplayName(), "Fake IDP" + r);

        // (1)  oidc, idp, idpName
        XMLMap rMap = getDBSClient().doGet(GET_USER, userMap);
        String oldSS = rMap.getString(userKeys.serialString());
        assert getStatusKey(rMap) == StatusCodes.STATUS_NEW_USER;
        assert !rMap.containsKey(distinguishedNameField) : unexpectedDNMessage;

        // (2) add display name
        userMap.put(userKeys.displayName(), firstName + " " + lastName);
        checkNoChange(userMap, oldSS);

        // (3) add an email. Should not return DN nor should SS update.
        userMap.put(userKeys.email(), firstName + "@woof.com");
        checkUpdated(userMap, oldSS, false);
        // cleanup
        getDBSClient().removeUser(rMap.getIdentifier(userKeys.userID()));

    }

    /**
     * Checks that once the DN is computed with first and last name, adding a display name does
     * not change the computed DN name or the serial string
     *
     * @throws Exception
     */
    @Test
    public void testNoChangeToDNAfterUpdates() throws Exception {
        String r = getRandomString();
        XMLMap userMap = new XMLMap();
        String oidc = System.currentTimeMillis() + r;
        String firstName = "Mohammed";
        String lastName = "Chang";
        userMap.put(userKeys.oidc(), oidc);
        userMap.put(userKeys.idp(), "https://idp.google.com/idp/" + r);
        userMap.put(userKeys.idpDisplayName(), "Fake IDP" + r);
        userMap.put(userKeys.displayName(), firstName + " " + lastName);
        userMap.put(userKeys.email(), firstName + "@woof.org");

        // Should create a user and return it.
        XMLMap rMap = getDBSClient().doGet(GET_USER, userMap);
        assert getStatusKey(rMap) == StatusCodes.STATUS_NEW_USER;
        assert rMap.containsKey(distinguishedNameField) : missingDNMessage;
        String oldSS = rMap.getString(userKeys.serialString());
        String oldDN = rMap.getString(distinguishedNameField);

        userMap.put(userKeys.firstName(), firstName + r);
        userMap.put(userKeys.lastName(), lastName);


        rMap = getDBSClient().doGet(GET_USER, userMap);
//        oldSS = rMap.getString(userKeys.serialString());
        assert getStatusKey(rMap) == StatusCodes.STATUS_OK;
        assert rMap.containsKey(distinguishedNameField) : missingDNMessage;
        assert rMap.getString(distinguishedNameField).equals(oldDN);
        assert rMap.getString(userKeys.serialString()).equals(oldSS);


        // Final check. Updating the  the first and last name does not change how the DN is computed
        userMap.put(userKeys.firstName(), firstName + "2" + r);
        userMap.put(userKeys.lastName(), lastName + "2");
        UserConverter uc = new UserConverter(userKeys, null);
        User user = new User(null, null);
        rMap = getDBSClient().doGet(GET_USER, userMap);
        uc.fromMap(rMap, user);
        DNState state = user.getDNState();
        oldSS = rMap.getString(userKeys.serialString());
        assert getStatusKey(rMap) == StatusCodes.STATUS_OK; // the 1st and last names changed
        assert rMap.containsKey(distinguishedNameField) : missingDNMessage;
        assert rMap.getString(distinguishedNameField).equals(oldDN);
        assert rMap.getString(userKeys.serialString()).equals(oldSS);
        // cleanup
        getDBSClient().removeUser(rMap.getIdentifier(userKeys.userID()));

    }

    /**
     * Checks that once the DN is computed with display name name, adding a first and last name does
     * not change the computed DN name or the serial string
     *
     * @throws Exception
     */
    @Test
    public void testNoChangeToDNAfterUpdates2() throws Exception {
        String r = getRandomString();
        XMLMap userMap = new XMLMap();
        String oidc = System.currentTimeMillis() + r;
        String firstName = "Mohammed";
        String lastName = "Chang";
        userMap.put(userKeys.oidc(), oidc);
        userMap.put(userKeys.idp(), "https://idp.google.com/idp/" + r);
        userMap.put(userKeys.idpDisplayName(), "Fake IDP" + r);
        userMap.put(userKeys.firstName(), firstName + r);
        userMap.put(userKeys.lastName(), lastName);

        userMap.put(userKeys.email(), firstName + "@woof.org");

        // Should create a user and return it.
        XMLMap rMap = getDBSClient().doGet(GET_USER, userMap);
        assert getStatusKey(rMap) == StatusCodes.STATUS_NEW_USER;
        assert rMap.containsKey(distinguishedNameField) : missingDNMessage;
        String oldSS = rMap.getString(userKeys.serialString());
        String oldDN = rMap.getString(distinguishedNameField);

        // Now that the DN can be computed with the display name, update the first and last name.
        // This should have no effect on the DN.
        userMap.put(userKeys.displayName(), firstName + " " + lastName);
        rMap = checkUpdated(userMap, oldSS, false);
        assert rMap.getString(distinguishedNameField).equals(oldDN);
        rMap = getDBSClient().getUser(rMap.getIdentifier(userKeys.userID()));
        // double check it really is stored ok.
        assert rMap.getString(userKeys.displayName()).equals(firstName + " " + lastName);
        assert rMap.getString(userKeys.firstName()).equals(firstName + r);
        assert rMap.getString(userKeys.lastName()).equals(lastName);
        // cleanup
        getDBSClient().removeUser(rMap.getIdentifier(userKeys.userID()));

    }

    /**
     * Checks that only first and last name are updated for FNAL users regardless of what the display name is
     * and that it is an error not supplying first and last name initially.
     *
     * @throws Exception
     */
    @Test
    public void testFNALRegression() throws Exception {
        String r = getRandomString();
        XMLMap userMap = new XMLMap();
        String firstName = "Mohammed";
        String lastName = "Chang";
        String eppn = firstName + "." + lastName + r + "@fnal.gov";
        UserKeys userKeys = new UserKeys();
        userMap.put(userKeys.eppn(), eppn);
        userMap.put(userKeys.email(), eppn);  // not true in general, ok for test.
        userMap.put(userKeys.organizationalUnit(), "Robots:minos27.fnal.gov:cron");  // not true in general, ok for test.
        userMap.put(userKeys.idp(), "https://idp.fnal.gov/idp/shibboleth");
        userMap.put(userKeys.idpDisplayName(), "Fermi National Accelerator Laboratory" + r);
        userMap.put(userKeys.displayName(), firstName + r + " " + lastName);
        XMLMap rMap = getDBSClient().doGet(GET_USER, userMap);
        String oldSS = rMap.getString(userKeys.serialString());
        assert getStatusKey(rMap) == StatusCodes.STATUS_NEW_USER;
        assert !rMap.containsKey(distinguishedNameField) : missingDNMessage;  // FNAL users require first and last name, so no DN is possible.

        userMap.put(userKeys.firstName, firstName);
        userMap.put(userKeys.lastName, lastName);
        rMap = getDBSClient().doGet(GET_USER, userMap);
        assert oldSS.equals(rMap.getString(userKeys.serialString()));
        assert getStatusKey(rMap) == StatusCodes.STATUS_OK;
        assert rMap.containsKey(distinguishedNameField) : missingDNMessage;  // FNAL users require first and last name, so no DN is possible.
        // cleanup
        getDBSClient().removeUser(rMap.getIdentifier(userKeys.userID()));
    }


    //Next up check that DNs created do not change if the underlying data changes.
    // After that, check that pairwise and subject id all are treated just like EPTID and EPPN. These form a set
    @Test
    public void testEptidMismatch() throws Exception {
        String r = getRandomString();
        XMLMap userMap = new XMLMap();

        userMap.put(userKeys.eptid(), "https://idp.bigstate.edu/idp/shibboleth/" + userKeys.eptid() + "/" + r);
        userMap.put(userKeys.idp(), "https://idp.bigstate.edu/idp/shibboleth");
        String firstName = "Mohammed";
        String lastName = "Chang";

        String eppn = firstName + r + "." + lastName + r + "@bigstate.edu";

        userMap.put(userKeys.eppn(), eppn);
        userMap.put(userKeys.email(), eppn);  // not true in general, ok for test.
        userMap.put(userKeys.organizationalUnit(), "test:org/unit");  // not true in general, ok for test.
        userMap.put(userKeys.idpDisplayName(), "Big State University");
        userMap.put(userKeys.displayName(), firstName + r + " " + lastName);
        // The test is to create a user
        XMLMap rMap = getDBSClient().doGet(GET_USER, userMap);
        assert getStatusKey(rMap) == StatusCodes.STATUS_NEW_USER;
        // change the eptid. Should get an error.
        userMap.put(userKeys.eptid(), "https://idp.bigstate.edu/idp/shibboleth/" + userKeys.eptid() + "42/" + r);
        try {
            rMap = getDBSClient().doGet(GET_USER, userMap);
            assert false;
        } catch (DBServiceException x) {
            // Note we cannot get a failure code directly using the doGet method. Normally this would just be
            // a returned value, but the doGet traps it and throws this exception.
            assert x.getStatusCode() == StatusCodes.STATUS_EPTID_MISMATCH : "status code is " + x.getStatusCode() + ", expected " + StatusCodes.STATUS_EPTID_MISMATCH;
        }

        // cleanup
        getDBSClient().removeUser(rMap.getIdentifier(userKeys.userID()));

    }

    @Test
    public void testSubjectIDMismatch() throws Exception {
        String r = getRandomString();
        XMLMap userMap = new XMLMap();

        userMap.put(userKeys.subjectId(), "https://idp.bigstate.edu/idp/shibboleth/" + userKeys.subjectId() + "/" + r);
        userMap.put(userKeys.idp(), "https://idp.bigstate.edu/idp/shibboleth");
        String firstName = "Mohammed";
        String lastName = "Chang";

        String eppn = firstName + r + "." + lastName + r + "@bigstate.edu";

        userMap.put(userKeys.eppn(), eppn);
        userMap.put(userKeys.email(), eppn);  // not true in general, ok for test.
        userMap.put(userKeys.organizationalUnit(), "test:org/unit");  // not true in general, ok for test.
        userMap.put(userKeys.idpDisplayName(), "Big State University");
        userMap.put(userKeys.displayName(), firstName + r + " " + lastName);
        // The test is to create a user
        XMLMap rMap = getDBSClient().doGet(GET_USER, userMap);
        assert getStatusKey(rMap) == StatusCodes.STATUS_NEW_USER;
        // change the eptid. Should get an error.
        userMap.put(userKeys.subjectId(), "https://idp.bigstate.edu/idp/shibboleth/" + userKeys.subjectId() + "42/" + r);
        try {
            rMap = getDBSClient().doGet(GET_USER, userMap);
            assert false;
        } catch (DBServiceException x) {
            // Note we cannot get a failure code directly using the doGet method. Normally this would just be
            // a returned value, but the doGet traps it and throws this exception.
            assert x.getStatusCode() == StatusCodes.STATUS_SUBJECT_ID_MISMATCH : "status code is " + x.getStatusCode() + ", expected " + StatusCodes.STATUS_SUBJECT_ID_MISMATCH;
        }


        // cleanup
        getDBSClient().removeUser(rMap.getIdentifier(userKeys.userID()));

    }

    @Test
    public void testPairwiseIDMismatch() throws Exception {
        String r = getRandomString();
        XMLMap userMap = new XMLMap();

        userMap.put(userKeys.pairwiseId(), "https://idp.bigstate.edu/idp/shibboleth/" + userKeys.pairwiseId() + "/" + r);
        userMap.put(userKeys.idp(), "https://idp.bigstate.edu/idp/shibboleth");
        String firstName = "Mohammed";
        String lastName = "Chang";

        String eppn = firstName + r + "." + lastName + r + "@bigstate.edu";

        userMap.put(userKeys.eppn(), eppn);
        userMap.put(userKeys.email(), eppn);  // not true in general, ok for test.
        userMap.put(userKeys.organizationalUnit(), "test:org/unit");  // not true in general, ok for test.
        userMap.put(userKeys.idpDisplayName(), "Big State University");
        userMap.put(userKeys.displayName(), firstName + r + " " + lastName);
        // The test is to create a user
        XMLMap rMap = getDBSClient().doGet(GET_USER, userMap);
        assert getStatusKey(rMap) == StatusCodes.STATUS_NEW_USER;
        // change the eptid. Should get an error.
        userMap.put(userKeys.pairwiseId(), "https://idp.bigstate.edu/idp/shibboleth/" + userKeys.pairwiseId() + "42/" + r);
        try {
            rMap = getDBSClient().doGet(GET_USER, userMap);
            assert false;
        } catch (DBServiceException x) {
            // Note we cannot get a failure code directly using the doGet method. Normally this would just be
            // a returned value, but the doGet traps it and throws this exception.
            assert x.getStatusCode() == StatusCodes.STATUS_PAIRWISE_ID_MISMATCH : "status code is " + x.getStatusCode() + ", expected " + StatusCodes.STATUS_PAIRWISE_ID_MISMATCH;
        }

        // cleanup
        getDBSClient().removeUser(rMap.getIdentifier(userKeys.userID()));

    }

    /**
     * Test for github. Sending all parameters and an oidc identifier should return a DN based on first and last name.
     *
     * @throws Exception
     */
    @Test
    public void testGithubUser() throws Exception {
        String r = getRandomString();
        XMLMap userMap = new XMLMap();
        String firstName = "TERRENCE";
        String lastName = "FLEURY";
        String oidc = System.currentTimeMillis() + r; // makes sure that this is a unique id
        userMap.put(userKeys.oidc(), oidc);
        userMap.put(userKeys.email(), "terrencegf@gmail.com");
        userMap.put(userKeys.useUSinDN(), "1");  // not true in general, ok for test.
        userMap.put(userKeys.idp(), "http://github.com/login/oauth/authorize");
        userMap.put(userKeys.idpDisplayName(), "GitHub");
        userMap.put(userKeys.displayName(), "Terrence Fleury");
        userMap.put(userKeys.firstName(), firstName);
        userMap.put(userKeys.lastName(), lastName);
        XMLMap rMap = getDBSClient().doGet(GET_USER, userMap);
        assert rMap.containsKey(distinguishedNameField);
        String oldDN = rMap.getString(distinguishedNameField);
        System.out.println(oldDN);
        String oldSS = rMap.getString(userKeys.serialString());

        userMap.put(userKeys.displayName(), "Terrence Fleury" + r);
        rMap = checkUpdated(userMap, oldSS, false);
        assert rMap.getString(distinguishedNameField).equals(oldDN);
        assert rMap.getString(distinguishedNameField).contains(DNUtil.encodeCertName(new String[]{firstName, lastName}));

        // cleanup
        getDBSClient().removeUser(rMap.getIdentifier(userKeys.userID()));
    }

    @Test
    public void testGithubUserRegression(UserStore userStore) throws Exception {
        String r = getRandomString();
        XMLMap userMap = new XMLMap();
        String firstName = "TERRENCE";
        String lastName = "FLEURY";
        // SO add a user with display name and first name, but no last name.
        String oidc = System.currentTimeMillis() + r; // makes sure that this is a unique id
        userMap.put(userKeys.oidc(), oidc);
        userMap.put(userKeys.email(), "terrencegf@gmail.com");
        userMap.put(userKeys.useUSinDN(), "1");  // not true in general, ok for test.
        userMap.put(userKeys.idp(), "http://github.com/login/oauth/authorize");
        userMap.put(userKeys.idpDisplayName(), "GitHub");
        // Funny characters so that if the encoding is off, there will be an error to catch later
        String displayName = "Tërrence` Flëury";
        userMap.put(userKeys.displayName(), displayName);
        userMap.put(userKeys.firstName(), firstName);
        // NO Last Name
        XMLMap rMap = getDBSClient().doGet(GET_USER, userMap);
        assert rMap.containsKey(distinguishedNameField);
        String oldDN = rMap.getString(distinguishedNameField);
        System.out.println(oldDN);

        // The test is to re-add this user with  partial information.
        userMap = new XMLMap();
        userMap.put(userKeys.idp(), "http://github.com/login/oauth/authorize");
        userMap.put(userKeys.idpDisplayName(), "GitHub");
        userMap.put(userKeys.displayName(), displayName);
        // No First name
        userMap.put(userKeys.lastName(), lastName);
        userMap.put(userKeys.email(), "terrencegf@gmail.com");
        userMap.put(userKeys.oidc(), oidc);
        userMap.put(userKeys.useUSinDN(), "1");  // not true in general, ok for test.
        rMap = getDBSClient().doGet(GET_USER, userMap);
        assert rMap.containsKey(distinguishedNameField);
        System.out.println(rMap.getString(distinguishedNameField));

        assert oldDN.equals(rMap.getString(distinguishedNameField));

        // cleanup
        getDBSClient().removeUser(rMap.getIdentifier(userKeys.userID()));
    }

    @Test
    public void testGHUserRegression2() throws Exception {

        // Send partial data -- no email
        String r = getRandomString();
        XMLMap userMap = new XMLMap();
        String firstName = "TERRENCE";
        String lastName = "FLEURY";
        // SO add a user with display name and first name, but no last name.
        String oidc = System.currentTimeMillis() + r; // makes sure that this is a unique id
        userMap.put(userKeys.idp(), "http://github.com/login/oauth/authorize");
        userMap.put(userKeys.idpDisplayName(), "GitHub");
        // Funny characters so that if the encoding is off, there will be an error to catch later
        String displayName = "Tërrence` Flëury";
        userMap.put(userKeys.displayName(), displayName);
        userMap.put(userKeys.firstName(), firstName);
        userMap.put(userKeys.lastName(), lastName);
        userMap.put(userKeys.oidc(), oidc);
        userMap.put(userKeys.useUSinDN(), "1");  // not true in general, ok for test.
        XMLMap rMap = getDBSClient().doGet(GET_USER, userMap);
        assert !rMap.containsKey(distinguishedNameField);
        String oldSS = rMap.getString(userKeys.serialString);


        // send complete data. Test is "does this create a DN and no imcrement serial string?
        userMap = new XMLMap();
        userMap.put(userKeys.idp(), "http://github.com/login/oauth/authorize");
        userMap.put(userKeys.idpDisplayName(), "GitHub");
        userMap.put(userKeys.displayName(), displayName);
        userMap.put(userKeys.firstName(), firstName);
        userMap.put(userKeys.lastName(), lastName);
        userMap.put(userKeys.email(), "terrencegf@gmail.com");
        userMap.put(userKeys.oidc(), oidc);
        userMap.put(userKeys.useUSinDN(), "1");  // not true in general, ok for test.
        rMap = getDBSClient().doGet(GET_USER, userMap);
        assert rMap.containsKey(distinguishedNameField);
        assert oldSS.equals(rMap.getString(userKeys.serialString()));
        // check that the cert has the right names encoded
        assert rMap.getString(distinguishedNameField).contains(DNUtil.encodeCertName(new String[]{firstName, lastName}));

    }

    @Test
    public void testGithubRegression3() throws Exception {

        // setup -- no email or last name, has all other info
        String r = getRandomString();
        XMLMap userMap = new XMLMap();
        String firstName = "TERRENCE";
        String lastName = "FLEURY";
        // SO add a user with display name and first name, but no last name.
        String oidc = System.currentTimeMillis() + r; // makes sure that this is a unique id
        userMap.put(userKeys.idp(), "http://github.com/login/oauth/authorize");
        userMap.put(userKeys.idpDisplayName(), "GitHub");
        // Funny characters so that if the encoding is off, there will be an error to catch later
        String displayName = "Tërrence` Flëury";
        userMap.put(userKeys.displayName(), displayName);
        userMap.put(userKeys.firstName(), firstName);
        userMap.put(userKeys.oidc(), oidc);
        userMap.put(userKeys.useUSinDN(), "1");  // not true in general, ok for test.
        XMLMap rMap = getDBSClient().doGet(GET_USER, userMap);
        assert !rMap.containsKey(distinguishedNameField);
        String oldSS = rMap.getString(userKeys.serialString);


        // add email and first name but still no last name. Should create a cert now with display name
        userMap = new XMLMap();
        userMap.put(userKeys.idp(), "http://github.com/login/oauth/authorize");
        userMap.put(userKeys.idpDisplayName(), "GitHub");
        userMap.put(userKeys.displayName(), displayName);
        userMap.put(userKeys.firstName(), firstName);
        userMap.put(userKeys.oidc(), oidc);
        userMap.put(userKeys.email(), "terrencegf@gmail.com");
        userMap.put(userKeys.useUSinDN(), "1");  // not true in general, ok for test.
        rMap = getDBSClient().doGet(GET_USER, userMap);
        assert rMap.containsKey(distinguishedNameField);
        assert oldSS.equals(rMap.getString(userKeys.serialString()));
        String oldDN = rMap.getString(distinguishedNameField);
        assert oldDN.contains(DNUtil.encodeCertName(new String[]{displayName}));

        System.out.println(rMap.getString(distinguishedNameField));

        // Finally, update first name but nothing else. This should change nothing.
        userMap = new XMLMap();
        userMap.put(userKeys.idp(), "http://github.com/login/oauth/authorize");
        userMap.put(userKeys.idpDisplayName(), "GitHub");
        userMap.put(userKeys.firstName(), firstName + r);
        userMap.put(userKeys.oidc(), oidc);
        userMap.put(userKeys.useUSinDN(), "1");  // not true in general, ok for test.
        rMap = getDBSClient().doGet(GET_USER, userMap);
        assert rMap.containsKey(distinguishedNameField);
        assert oldSS.equals(rMap.getString(userKeys.serialString()));
        assert oldDN.equals(rMap.getString(distinguishedNameField));

    }

    /**
     * Runs through a standard lifecycle for each id with updates and such
     * @throws Exception
     */
    @Test
    public void testAllIds() throws Exception {
        lifecycleTest(userKeys.eptid());
        lifecycleTest(userKeys.oidc());
        lifecycleTest(userKeys.eppn());
        lifecycleTest(userKeys.openID());
        lifecycleTest(userKeys.pairwiseId());
        lifecycleTest(userKeys.subjectId());
    }

    /**
     * The id is EPTID, EPPN, etc. All of them get tested with this
     * @param id
     * @throws Exception
     */
    public void lifecycleTest(String id) throws Exception {
        // setup -- no email or last name, has all other info
        String r = getRandomString();
        XMLMap userMap = new XMLMap();
        String firstName = "TERRENCE";
        String lastName = "FLEURY";
        String idp = "http://random.com/login/oauth/authorize/" + r;
        String idpName = "Random IDP " + id + " " + r;
        // Funny characters so that if the encoding is off, there will be an error to catch later
        String displayName = "Tërrence d`Flëury";
        String email = "terrencegf@gmail.com";
        String idValue = r + ":" + System.currentTimeMillis(); // makes sure that this is a unique id

        // SO add a user with display name and first name, but no last name.
        userMap.put(userKeys.idp(), idp);
        userMap.put(userKeys.idpDisplayName(), idpName);

        userMap.put(userKeys.displayName(), displayName);
        userMap.put(userKeys.firstName(), firstName);
        userMap.put(id, idValue);
        userMap.put(userKeys.useUSinDN(), "1");  // not true in general, ok for test.
        XMLMap rMap = getDBSClient().doGet(GET_USER, userMap);
        assert !rMap.containsKey(distinguishedNameField);
        String oldSS = rMap.getString(userKeys.serialString);

        // add email and first name but still no last name. Should create a cert now with display name
        userMap = new XMLMap();
        userMap.put(userKeys.idp(), idp);
        userMap.put(userKeys.idpDisplayName(), idpName);
        userMap.put(userKeys.displayName(), displayName);
        userMap.put(userKeys.firstName(), firstName);
        userMap.put(id, idValue);
        userMap.put(userKeys.email(), email);
        userMap.put(userKeys.useUSinDN(), "1");  // not true in general, ok for test.
        rMap = getDBSClient().doGet(GET_USER, userMap);
        assert rMap.containsKey(distinguishedNameField);
        assert oldSS.equals(rMap.getString(userKeys.serialString()));
        String oldDN = rMap.getString(distinguishedNameField);
        assert oldDN.contains(DNUtil.encodeCertName(new String[]{displayName}));

        System.out.println(rMap.getString(distinguishedNameField));

        // Finally, update first name but nothing else. This should change nothing.
        userMap = new XMLMap();
        userMap.put(userKeys.idp(), idp);
        userMap.put(userKeys.idpDisplayName(), idpName);
        userMap.put(userKeys.firstName(), firstName + r);
        userMap.put(id, idValue);
        userMap.put(userKeys.useUSinDN(), "1");  // not true in general, ok for test.
        rMap = getDBSClient().doGet(GET_USER, userMap);
        assert rMap.containsKey(distinguishedNameField);
        assert oldSS.equals(rMap.getString(userKeys.serialString()));
        assert oldDN.equals(rMap.getString(distinguishedNameField));

        // add a last name. At this point, all fields for the user are filled in,
        // but the display name should still be used and no changes to serial string should happen
        userMap = new XMLMap();
        userMap.put(userKeys.idp(), idp);
        userMap.put(userKeys.idpDisplayName(), idpName);
        userMap.put(userKeys.lastName(), lastName);
        userMap.put(id, idValue);
        userMap.put(userKeys.useUSinDN(), "1");  // not true in general, ok for test.
        rMap = getDBSClient().doGet(GET_USER, userMap);
        assert rMap.containsKey(distinguishedNameField);
        assert oldSS.equals(rMap.getString(userKeys.serialString()));
        assert oldDN.equals(rMap.getString(distinguishedNameField));

        // Ditto, but only updatelast name (shows not used here)
        userMap.put(userKeys.idp(), idp);
        userMap.put(userKeys.idpDisplayName(), idpName);
        userMap.put(userKeys.lastName(), lastName + r);
        userMap.put(id, idValue);
        userMap.put(userKeys.useUSinDN(), "1");  // not true in general, ok for test.
        rMap = getDBSClient().doGet(GET_USER, userMap);
        assert rMap.containsKey(distinguishedNameField);
        assert oldSS.equals(rMap.getString(userKeys.serialString()));
        assert oldDN.equals(rMap.getString(distinguishedNameField));
    }
}
