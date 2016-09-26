package org.cilogon.d2.storage.impl.memorystore;

import edu.uiuc.ncsa.security.core.IdentifiableProvider;
import edu.uiuc.ncsa.security.storage.MemoryStore;
import org.cilogon.d2.storage.IdentityProvider;
import org.cilogon.d2.storage.IdentityProviderStore;

import java.util.Collection;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/13/12 at  2:34 PM
 */
public class MemoryIDPStore extends MemoryStore<IdentityProvider> implements IdentityProviderStore {
    public MemoryIDPStore(IdentifiableProvider<IdentityProvider> identityProviderIdentifiableProvider) {
        super(identityProviderIdentifiableProvider);
    }

    @Override
    public void add(IdentityProvider idp) {
        put(idp.getIdentifier(), idp);
    }

    @Override
    public void replaceAll(Collection<IdentityProvider> idps) {
        clear();
        add(idps);
    }

    @Override
    public void add(Collection<? extends IdentityProvider> idps) {
        for (IdentityProvider idp : idps) {
            if (!containsKey(idp)) {
                add(idp);
            }
        }
    }

    @Override
    public boolean hasIdp(String idp) {
        return containsKey(idp);
    }


}
