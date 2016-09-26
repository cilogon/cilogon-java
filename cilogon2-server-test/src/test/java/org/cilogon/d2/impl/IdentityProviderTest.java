package org.cilogon.d2.impl;

import edu.uiuc.ncsa.security.core.Identifier;
import org.cilogon.d2.CILStoreTest;
import org.cilogon.d2.storage.IdentityProvider;
import org.cilogon.d2.storage.IdentityProviderStore;
import org.junit.Test;

import java.net.URI;
import java.util.*;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/9/12 at  11:36 AM
 */
public abstract class IdentityProviderTest extends CILStoreTest {
    @Override
    public void checkStoreClass() throws Exception {
        testClassAsignability(getIDP());
    }

    public IdentityProviderStore getIDP() throws Exception {
        return getCILStoreTestProvider().getIDP();
    }

    /**
     * Test that OAUTH-138 works, i.e., that adding new items to a store does not remove any existing items and
     * that if an item that already exists is added, then no error results. This is a change to the original
     * contract which specified that adding items would completely remove existing items and overwrite them with
     * the new list.
     *
     * @throws Exception
     */
    @Test
    public void testAddUnique() throws Exception {
        // Create a collection of random IDPs and add a few existing ones. Invoking add all should preserve any that
        // were in the store already and just add the new ones.
        Collection<IdentityProvider> all = getIDP().values();
        HashMap<Identifier, IdentityProvider> idps = new HashMap<>();
        String rs = getRandomString();
        for (int i = 0; i < count; i++) {
            IdentityProvider idp = new IdentityProvider(URI.create("urn:test:identity/provider/" + rs + "/" + i));
            idps.put(idp.getIdentifier(), idp);
        }
        Iterator<IdentityProvider> iterator = all.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            IdentityProvider idp = iterator.next();
            if (0 == i++ % 2) idps.put(idp.getIdentifier(), idp);
        }
        int size = getIDP().size();

        getIDP().putAll(idps);
        assert getIDP().size() == size + count : "Incorrect number of idps added. Expected to have " + (size + count) + " and found " + getIDP().size() + " instead.";
        // now we have to check that each of these is in the store.
        for (Identifier idp : idps.keySet()) {
            assert getIDP().containsKey(idp) : "Error: the store does not contain idp =" + idp;
        }
        IdentityProvider idp = new IdentityProvider(URI.create("urn:test:identity/provider/" + getRandomString()));

        assert !getIDP().containsKey(idp) : "Error: IDP store tests positive for an entry that it cannot have.";
        for (IdentityProvider idp2 : all) {
            assert getIDP().containsKey(idp2.getIdentifier()) : "Error: the store does not contain idp =" + idp;
        }
    }

    @Test
    public void testAdd() throws Exception {
        LinkedList<IdentityProvider> idps = new LinkedList<IdentityProvider>();
        for (int i = 0; i < count; i++) {
            idps.add(new IdentityProvider(URI.create("urn:test:identity/provider/" + System.currentTimeMillis() + "/" + i)));
        }
        IdentityProvider firstIdp = idps.getFirst();
        assert !getIDP().hasIdp(firstIdp.getIdentifierString());
        // check list operations.
        getIDP().add(idps);
        Set<Identifier> returnedIDPs = getIDP().keySet();
        for (Object ooo : returnedIDPs.toArray()) {
        }
        for (IdentityProvider idp : idps) {
            assert returnedIDPs.contains(idp.getIdentifier()) : "Missing the idp = \"" + idp + "\"";
        }
        getIDP().remove(firstIdp.getIdentifier());

        assert !getIDP().containsValue(firstIdp);
        assert !getIDP().hasIdp(firstIdp.getIdentifierString());
        getIDP().replaceAll(idps);
        assert getIDP().size() == idps.size();
    }
}
