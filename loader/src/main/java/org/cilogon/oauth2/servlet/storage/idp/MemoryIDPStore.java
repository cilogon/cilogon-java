package org.cilogon.oauth2.servlet.storage.idp;

import edu.uiuc.ncsa.security.core.IdentifiableProvider;
import edu.uiuc.ncsa.security.core.XMLConverter;
import edu.uiuc.ncsa.security.storage.data.MapConverter;
import edu.uiuc.ncsa.security.storage.monitored.MonitoredMemoryStore;

import java.util.Collection;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/13/12 at  2:34 PM
 */
public class MemoryIDPStore extends MonitoredMemoryStore<IdentityProvider> implements IdentityProviderStore {
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

    public MapConverter getMapConverter() {
        IDPKeys keys = new IDPKeys();
        return new IDPConverter(keys,identifiableProvider);
    }

    @Override
    public XMLConverter<IdentityProvider> getXMLConverter() {
        return getMapConverter();
    }

    @Override
    public List<IdentityProvider> getMostRecent(int n, List<String> attributes) {
        throw new UnsupportedOperationException();
    }
}
