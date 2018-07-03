package org.cilogon.d2.twofactor;

import edu.uiuc.ncsa.security.core.IdentifiableProvider;
import edu.uiuc.ncsa.security.storage.MemoryStore;
import edu.uiuc.ncsa.security.storage.data.MapConverter;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/18/12 at  10:15 AM
 */
public class TwoFactorMS extends MemoryStore<TwoFactorInfo> implements TwoFactorStore{
    public TwoFactorMS(IdentifiableProvider<TwoFactorInfo> identifiableProvider) {
        super(identifiableProvider);
    }

    @Override
    public MapConverter getConverter() {
        return new TwoFactorMapConverter((TwoFactorInfoProvider) identifiableProvider);
    }
}
