package test.cilogon.util;

import edu.uiuc.ncsa.security.core.util.DateUtils;
import edu.uiuc.ncsa.security.core.util.DoubleHashMap;
import edu.uiuc.ncsa.security.delegation.token.*;
import edu.uiuc.ncsa.security.delegation.token.impl.OA1AccessTokenImpl;
import edu.uiuc.ncsa.security.delegation.token.impl.OA1AuthorizationGrantImpl;
import edu.uiuc.ncsa.security.util.TestBase;
import org.cilogon.oauth2.servlet.util.SerialStrings;
import org.junit.Test;
import test.cilogon.CILTestStoreProviderI2;

import java.net.URI;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/16/12 at  10:34 AM
 */
public  class TokenTest extends TestBase {


    protected void checkToken(Token token, String testServer, String component, String ssComponent) {
        assert token.getToken().startsWith(testServer);
        assert token.getToken().substring(testServer.length() + 1).startsWith(component);
        DateUtils.getDate(token.getToken());
        String sharedSecret = null;
        if (token instanceof OA1AccessTokenImpl) {
            sharedSecret = ((OA1AccessTokenImpl) token).getSharedSecret();
        } else if (token instanceof OA1AuthorizationGrantImpl) {
            sharedSecret = ((OA1AuthorizationGrantImpl) token).getSharedSecret();
        }
        if (sharedSecret == null) return;
        assert ssComponent != null : "Error: test not correct. To test for a shared secret you must supply a component for the token.";
        assert sharedSecret.startsWith(testServer);
        DateUtils.getDate(sharedSecret);
        assert sharedSecret.substring(testServer.length() + 1).startsWith(ssComponent);
    }


    @Test
    public void testAccessToken(CILTestStoreProviderI2 provider) throws Exception {
        TokenForge tf = provider.getTokenForge();
        AuthorizationGrant ag = tf.getAuthorizationGrant();
        AccessToken token = tf.getAccessToken("urn:test:/test/accessToken/" + System.currentTimeMillis(), "urn:test:/test/accessToken/SharedSecret/" + System.currentTimeMillis());
        Verifier v = tf.getVerifier("urn:test:/test/verfier/" + System.currentTimeMillis());
        System.out.println(ag);
        System.out.println(token);
        System.out.println(v);

        // This just checks that there are no errors parsing the dates.
        try {
            DateUtils.getDate(token.getToken());
            DateUtils.getDate(v.getToken());
            assert true;
        } catch (Exception x) {
            assert false : "Error parsing the dates: \"" + x.getMessage() + "\"";
        }

    }

    @Test
    public void testMunging() throws Exception {
        DoubleHashMap<URI, String> hashMap = new DoubleHashMap<URI, String>();

        String prefix0 = "A";
        URI ns0 = URI.create("urn:test:foo");
        hashMap.put(ns0, prefix0);
        // add a red herring -- this should never get used.
        String prefix1 = "B";
        URI ns1 = URI.create("urn:test:foo2");
        hashMap.put(ns1, prefix1);

        SerialStrings ss = new SerialStrings(hashMap);

        URI target = URI.create("urn:test:foo/bar/baz");
        URI expectedResult = URI.create(prefix0 + ":bar/baz");
        URI x = ss.mungePrefix(ns0, target);
        URI demunge = ss.demungePrefix(ns0, expectedResult);
        assert x.equals(expectedResult); // x munges to expected result
        assert target.equals(demunge); // demunged expected result is the same as the original argument
    }
}
