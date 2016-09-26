package org.cilogon.d2.util;

import edu.uiuc.ncsa.security.core.util.IdentifiableProviderImpl;
import edu.uiuc.ncsa.security.delegation.server.storage.ClientStore;
import edu.uiuc.ncsa.security.delegation.storage.Client;
import edu.uiuc.ncsa.security.delegation.token.TokenForge;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 2/28/14 at  3:08 PM
 */
public class CILPGSTConverter<V extends CILogonServiceTransaction> extends CILServiceTransactionConverter<V> {
    /**
     * Constructor just sets the correct transaction keys for the database. Otherwise this class does nothing
     * @param identifiableProvider
     * @param tokenForge
     * @param cs
     */
    public CILPGSTConverter(IdentifiableProviderImpl identifiableProvider, TokenForge tokenForge, ClientStore<? extends Client> cs) {
        super(new CILPGTransactionKeys(), identifiableProvider, tokenForge, cs);
    }
}
