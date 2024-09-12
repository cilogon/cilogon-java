package org.cilogon.qdl.module.storage;

import org.cilogon.qdl.module.CILStoreFacade;
import org.oa4mp.server.qdl.storage.StoreAccessModule;
import org.oa4mp.server.qdl.storage.StoreFacade;
import org.qdl_lang.module.Module;
import org.qdl_lang.state.State;

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
        storeModule.storeFacade = newStoreFacade();
        doIt(storeModule, state);
        if(state != null){
            state.addLibEntry("cilogon", "store", getClass().getCanonicalName());
        }
        setupModule(storeModule);
        return storeModule;
    }

    @Override
    public StoreFacade newStoreFacade() {
        return new CILStoreFacade();
    }
    
}
