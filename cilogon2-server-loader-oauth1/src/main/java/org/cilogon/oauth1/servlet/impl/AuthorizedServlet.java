package org.cilogon.oauth1.servlet.impl;

import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.delegation.token.Token;
import net.oauth.OAuth;
import org.cilogon.d2.servlet.AbstractAuthorizedServlet;

import java.net.URI;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/23/13 at  11:32 AM
 */
public class AuthorizedServlet extends AbstractAuthorizedServlet {
    @Override
    public String createRedirect(URI baseUri, Identifier identifier, Token verifier) {
        return baseUri.toString() + "?" + OAuth.OAUTH_TOKEN + "=" + identifier + "&" + OAuth.OAUTH_VERIFIER + "=" + verifier.getToken();
    }
}
