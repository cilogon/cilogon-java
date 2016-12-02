package org.cilogon.d2.storage.provider;


import edu.uiuc.ncsa.myproxy.oa4mp.server.admin.transactions.DSTransactionProvider;
import org.cilogon.d2.util.CILogonServiceTransaction;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/5/12 at  12:34 PM
 */
public class CILTransactionProvider extends DSTransactionProvider<CILogonServiceTransaction> {
    @Override
    public CILogonServiceTransaction get(boolean createNewIdentifier) {
        return new CILogonServiceTransaction(createNewId(createNewIdentifier));
    }

}
