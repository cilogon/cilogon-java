package org.cilogon.d2.storage;

import edu.uiuc.ncsa.security.core.Store;
import edu.uiuc.ncsa.security.storage.data.MapConverter;

import java.util.Collection;

/**
 * <p>Created by Jeff Gaynor<br>
 * on Apr 10, 2010 at  8:26:33 AM
 */
public interface IdentityProviderStore extends Store<IdentityProvider> {
    /**
     * Replace every entry in the store by the ones in the list. At the
     * end of this operation, the store will be identical to the given list.
     *
     * @param idps
     */
    public void replaceAll(Collection<IdentityProvider> idps);

    /**
     * Add an identity provider, using the unique id as the key
     *
     * @param idp
     */
    public void add(IdentityProvider idp);



    /**
     * Adds a list of identity providers.
     * Note: Change to contract. Previously this was to fail if any of the idps were already in the store.
     * The problem is that if a list of these was sent that had fewer idps than previously, (such as due
     * to some error in a non-Java component) then the list of IDPs might be damaged.
     *
     * Therefore, now this should add any idps not already in the store, and ignore any that are there already.
     *
     * @param idps
     */
    public void add(Collection<? extends IdentityProvider> idps);

    /**
     * Check if there is an entry for the given unique id.
     *
     * @param idp
     * @return
     */
    public boolean hasIdp(String idp);

    public MapConverter getConverter();
}
