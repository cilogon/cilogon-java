package org.cilogon.oauth2.servlet.storage.idp;

import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.storage.monitored.Monitored;

import java.net.URI;

/**
 * <p>Created by Jeff Gaynor<br>
 * on Apr 1, 2010 at  1:01:31 PM
 */
public class IdentityProvider extends Monitored {     // fixes https://github.com/cilogon/cilogon-java/issues/42
    public IdentityProvider(Identifier identifier) {
        super(identifier);
    }

    public static final long serialVersionUID = 0xCafeD00dL;


    public IdentityProvider(URI uri) {
        super(new BasicIdentifier(uri));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IdentityProvider)) return false;
        IdentityProvider other = (IdentityProvider) obj;
        if(!other.getIdentifierString().equals(getIdentifierString())) return false;
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getIdentifierString() + "]";
    }
}
