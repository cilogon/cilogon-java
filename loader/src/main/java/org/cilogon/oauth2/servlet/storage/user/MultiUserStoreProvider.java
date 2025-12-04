package org.cilogon.oauth2.servlet.storage.user;

import edu.uiuc.ncsa.security.core.cf.CFNode;
import edu.uiuc.ncsa.security.core.configuration.provider.MultiTypeProvider;
import edu.uiuc.ncsa.security.core.util.IdentifiableProviderImpl;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import org.cilogon.oauth2.servlet.util.Incrementable;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/19/12 at  6:20 PM
 */
public class MultiUserStoreProvider extends MultiTypeProvider<UserStore> {
    Incrementable incrementable;
    IdentifiableProviderImpl<User> userProvider;

    public MultiUserStoreProvider(CFNode cn,
                                  boolean disableDefaultStore,
                                  MyLoggingFacade loggingFacade,
                                  IdentifiableProviderImpl<User> userProvider,
                                  Incrementable incrementable) {
        super(cn, disableDefaultStore, loggingFacade, null, null);
        this.userProvider = userProvider;
        this.incrementable = incrementable;
    }


    UserStore defaultStore = null;
    @Override
    public UserStore getDefaultStore() {
        // make sure that if a memory store is being used, the same one is returned consistently or there will
        // be multiple, inconsistent copies.
        if(defaultStore == null){
              logger.info("Using default in memory user store.");
            MemoryUserStore u = new MemoryUserStore(userProvider, incrementable);
            // no upkeep config in default store.
            defaultStore = u;
        }
        return defaultStore;
    }

}
