package org.cilogon.d2;

import edu.uiuc.ncsa.security.delegation.token.AccessToken;
import edu.uiuc.ncsa.security.delegation.token.AuthorizationGrant;
import edu.uiuc.ncsa.security.delegation.token.TokenForge;
import edu.uiuc.ncsa.security.delegation.token.Verifier;
import edu.uiuc.ncsa.security.oauth_1_0a.OAuthTokenForge;
import org.cilogon.d2.storage.provider.TokenPrefixProvider;
import org.cilogon.d2.util.CILogonConfiguration;
import org.cilogon.d2.util.SerialStringProvider;
import org.cilogon.d2.util.SerialStrings;
import org.cilogon.d2.util.TokenTest;
import org.cilogon.oauth1.loader.TokenForgeProvider;
import org.junit.Test;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 5/2/12 at  10:26 AM
 */
public class CILTokenTest extends TokenTest {

    @Test
    public void testTokenSemantics() throws Exception {
        String testServer = "http://cilogon.org/serverX";
        TokenPrefixProvider ssp = new TokenPrefixProvider(testServer);

        // This will check that the basic semantics of the tokens are ok.
        OAuthTokenForge oAuthTokenForge = new OAuthTokenForge(ssp.get());
        // here is where we make SURE that the semantics of the tokens do not
        // change.
        AuthorizationGrant ag = oAuthTokenForge.getAuthorizationGrant();
        checkToken(ag, testServer, oAuthTokenForge.tempCred, oAuthTokenForge.tempCredSS);

        AccessToken accessToken = oAuthTokenForge.getAccessToken();
        checkToken(accessToken, testServer, oAuthTokenForge.accessToken, oAuthTokenForge.accessTokenSS);

        Verifier verifier = oAuthTokenForge.getVerifier();
        checkToken(verifier, testServer, oAuthTokenForge.verifier, null);

    }

    @Test
    public void testProviders() throws Exception {
        CILogonConfiguration loader = (CILogonConfiguration) ServiceTestUtils.getPgStoreProvider().getConfigLoader();
        SerialStringProvider sp = loader.getSsp();
        SerialStrings ss = sp.get();
        System.out.println(ss);
        TokenPrefixProvider serverStringProvider = loader.getTokenPrefixProvider();
        System.out.println("Server string from config file = \"" + serverStringProvider.get() + "\"");
        TokenForgeProvider tfp = new TokenForgeProvider(serverStringProvider);
        TokenForge tokenForge = tfp.get();
        System.out.println("auth. grant = " + tokenForge.getAuthorizationGrant());
        System.out.println("access token = " + tokenForge.getAccessToken());
        System.out.println("verifier = " + tokenForge.getVerifier());

    }

}
