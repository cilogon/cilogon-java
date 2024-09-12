package org.cilogon.oauth2.servlet.storage.transaction;

import edu.uiuc.ncsa.security.core.util.IdentifiableProviderImpl;
import org.oa4mp.delegation.common.storage.clients.Client;
import org.oa4mp.delegation.common.token.TokenForge;
import org.oa4mp.delegation.server.storage.ClientStore;

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
