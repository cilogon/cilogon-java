package org.cilogon.oauth2.servlet.storage.idp;

import edu.uiuc.ncsa.security.core.IdentifiableProvider;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.storage.data.MapConverter;
import edu.uiuc.ncsa.security.storage.monitored.MonitoredFileStore;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/8/12 at  5:17 PM
 */
public class IDPFileStore extends MonitoredFileStore<IdentityProvider> implements IdentityProviderStore {
    public IDPFileStore(File file, IdentifiableProvider<IdentityProvider> idpp, IDPConverter converter,
                        boolean removeEmptyFiles,
                        boolean removeFailedFiles) {
        super(file, idpp, converter, removeEmptyFiles, removeFailedFiles);
    }

    public IDPFileStore(File storeDirectory,
                        File indexDirectory,
                        IdentifiableProvider<IdentityProvider> idpp,
                        MapConverter converter,
                        boolean removeEmptyFiles,
                        boolean removeFailedFiles) {
        super(storeDirectory, indexDirectory, idpp, converter, removeEmptyFiles, removeFailedFiles);
    }

    @Override
    public void add(IdentityProvider idp) {
        super.put(idp.getIdentifier(), idp);
    }

    @Override
    public void replaceAll(Collection<IdentityProvider> idps) {
        clear();
        for (IdentityProvider x : idps) {
            add(x);
        }
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
        IdentityProvider x = get(BasicIdentifier.newID(idp));
        if (x == null) {
            return false;
        }
        return x.getIdentifierString().equals(idp);
    }

    @Override
    public List<IdentityProvider> getMostRecent(int n, List<String> attributes) {
        throw new UnsupportedOperationException();
    }
}
