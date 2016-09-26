package org.cilogon.d2.impl.postgres;

import edu.uiuc.ncsa.security.delegation.server.ServiceTransaction;
import edu.uiuc.ncsa.security.delegation.server.storage.ClientApproval;
import edu.uiuc.ncsa.security.delegation.server.storage.ClientApprovalStore;
import edu.uiuc.ncsa.security.delegation.server.storage.ClientStore;
import edu.uiuc.ncsa.security.delegation.storage.Client;
import edu.uiuc.ncsa.security.delegation.storage.TransactionStore;
import org.cilogon.d2.CILTestStoreProvider;
import org.cilogon.d2.util.ArchivedUserStore;
import org.cilogon.d2.util.Incrementable;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/22/12 at  11:47 AM
 */
public abstract class PGStoreProvider2 extends CILTestStoreProvider {
    @Override
    public ArchivedUserStore getArchivedUserStore() throws Exception {
        return getCILSE().getArchivedUserStore();
    }

    @Override
    public TransactionStore<ServiceTransaction> getTransactionStore() throws Exception {
        return getSE().getTransactionStore();
    }



    @Override
    public ClientStore<Client> getClientStore() throws Exception {
        return getSE().getClientStore();
    }

    @Override
    public ClientApprovalStore<ClientApproval> getClientApprovalStore() throws Exception {
        return getSE().getClientApprovalStore();
    }

    @Override
    public Incrementable getSequence() throws Exception {
        return getCILSE().getIncrementable();
    }
}
