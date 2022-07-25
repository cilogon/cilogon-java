package org.cilogon.oauth2.servlet.twofactor;

import edu.uiuc.ncsa.security.storage.data.MapConverter;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPool;
import edu.uiuc.ncsa.security.storage.sql.SQLStore;
import edu.uiuc.ncsa.security.storage.sql.internals.Table;

import javax.inject.Provider;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/18/12 at  10:15 AM
 */
public class TwoFactorSQLStore extends SQLStore<TwoFactorInfo> implements TwoFactorStore{
    public TwoFactorSQLStore(ConnectionPool connectionPool,
                             Table table,
                             Provider<TwoFactorInfo> identifiableProvider,
                             MapConverter<TwoFactorInfo> twoFactorInfoMapConverter) {
        super(connectionPool, table, identifiableProvider, twoFactorInfoMapConverter);
    }

    @Override
    public String getCreationTSField() {
        throw new UnsupportedOperationException();
    }
}
