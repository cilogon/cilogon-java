package org.cilogon.oauth1.loader;

import edu.uiuc.ncsa.myproxy.oa4mp.server.OA4MPConfigTags;
import edu.uiuc.ncsa.security.core.configuration.provider.CfgEvent;
import edu.uiuc.ncsa.security.core.configuration.provider.HierarchicalConfigProvider;
import edu.uiuc.ncsa.security.delegation.token.TokenForge;
import edu.uiuc.ncsa.security.oauth_1_0a.OAuthTokenForge;
import org.cilogon.d2.storage.provider.TokenPrefixProvider;

/**
 * This adds a marker to the identifiers to disambiguate tokens created here
 * (which include "delegation2") from CILogon version 1.0 tokens.
 * <p>Created by Jeff Gaynor<br>
 * on 4/11/12 at  10:48 AM
 */
public class TokenForgeProvider extends HierarchicalConfigProvider<TokenForge> implements OA4MPConfigTags {
    public static class CILTokenForge extends OAuthTokenForge {

        public CILTokenForge(String server) {
            super(server);
        }

        @Override
        protected void setup() {
            String d2 = "/delegation2";
            tempCred(super.tempCred() + d2);
            tempCredSS(super.tempCredSS() + d2);
            accessToken(super.accessToken() + d2);
            accessTokenSS(super.accessTokenSS() + d2);
            verifier(super.verifier() + d2);
            super.setup();
        }


    }

    public TokenForgeProvider(TokenPrefixProvider serverStringProvider) {
        this.serverStringProvider = serverStringProvider;
    }

    TokenPrefixProvider serverStringProvider;

    @Override
    protected boolean checkEvent(CfgEvent cfgEvent) {
        return false;
    }

    @Override
    public Object componentFound(CfgEvent configurationEvent) {
        if (checkEvent(configurationEvent)) {
            return get();
        }
        return null;
    }

    @Override
    public TokenForge get() {
        return new CILTokenForge(serverStringProvider.get());
    }
}
