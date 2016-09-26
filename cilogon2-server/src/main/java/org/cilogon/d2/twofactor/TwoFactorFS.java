package org.cilogon.d2.twofactor;

import edu.uiuc.ncsa.security.core.IdentifiableProvider;
import edu.uiuc.ncsa.security.storage.FileStore;
import edu.uiuc.ncsa.security.storage.data.MapConverter;

import java.io.File;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/18/12 at  9:22 AM
 */
public class TwoFactorFS extends FileStore<TwoFactorInfo> implements TwoFactorStore{

    public TwoFactorFS(File file, IdentifiableProvider<TwoFactorInfo> idp, MapConverter<TwoFactorInfo> cp) {
        super(file, idp, cp);
    }

    protected TwoFactorFS(File storeDirectory,
                          File indexDirectory,
                          IdentifiableProvider<TwoFactorInfo> identifiableProvider,
                          MapConverter<TwoFactorInfo> twoFactorInfoMapConverter) {
        super(storeDirectory, indexDirectory, identifiableProvider, twoFactorInfoMapConverter);
    }
}
