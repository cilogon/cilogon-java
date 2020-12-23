package org.cilogon.qdl.module.storage;

import edu.uiuc.ncsa.oa2.qdl.storage.StoreAccessModule;
import edu.uiuc.ncsa.qdl.module.Module;
import edu.uiuc.ncsa.qdl.state.State;
import org.cilogon.qdl.module.CILStoreFacade;

import java.net.URI;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 12/23/20 at  1:42 PM
 */
public class CILStoreModule extends StoreAccessModule {
    public CILStoreModule() {
    }

    public CILStoreModule(URI namespace, String alias) {
        super(namespace, alias);
    }

    @Override
    public Module newInstance(State state) {
        CILStoreModule storeModule = new CILStoreModule(URI.create("cilogon:/qdl/store"), "store");
        CILStoreFacade facade= new CILStoreFacade();
        doIt(storeModule, facade, state);
        return storeModule;
    }
}
