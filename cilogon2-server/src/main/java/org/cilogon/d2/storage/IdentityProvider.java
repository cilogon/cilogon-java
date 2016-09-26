package org.cilogon.d2.storage;

import edu.uiuc.ncsa.security.core.Identifiable;
import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.core.util.IdentifiableImpl;

import java.net.URI;

/**
 * <p>Created by Jeff Gaynor<br>
 * on Apr 1, 2010 at  1:01:31 PM
 */
public class IdentityProvider extends IdentifiableImpl implements Identifiable {
    public IdentityProvider(Identifier identifier) {
        super(identifier);
    }

    public static final long serialVersionUID = 0xCafeD00dL;


    public IdentityProvider(URI uri) {
        super(new BasicIdentifier(uri));
    }

    @Override
    public boolean equals(Object obj) {
        if(!super.equals(obj)) return false;
        if (!(obj instanceof IdentityProvider)) return false;
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getIdentifierString() + "]";
    }
}
