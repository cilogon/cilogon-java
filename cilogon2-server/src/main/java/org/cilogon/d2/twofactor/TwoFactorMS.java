package org.cilogon.d2.twofactor;

import edu.uiuc.ncsa.security.core.IdentifiableProvider;
import edu.uiuc.ncsa.security.storage.MemoryStore;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/18/12 at  10:15 AM
 */
public class TwoFactorMS extends MemoryStore<TwoFactorInfo> implements TwoFactorStore{
    public TwoFactorMS(IdentifiableProvider<TwoFactorInfo> identifiableProvider) {
        super(identifiableProvider);
    }
}
