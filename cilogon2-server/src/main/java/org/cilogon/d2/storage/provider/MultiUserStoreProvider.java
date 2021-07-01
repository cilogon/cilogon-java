package org.cilogon.d2.storage.provider;

import edu.uiuc.ncsa.security.core.configuration.provider.MultiTypeProvider;
import edu.uiuc.ncsa.security.core.util.IdentifiableProviderImpl;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.cilogon.d2.storage.User;
import org.cilogon.d2.storage.UserStore;
import org.cilogon.d2.storage.impl.memorystore.MemoryUserStore;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/19/12 at  6:20 PM
 */
public class MultiUserStoreProvider extends MultiTypeProvider<UserStore> {
    IdentifiableProviderImpl<User> userProvider;

    public MultiUserStoreProvider(ConfigurationNode cn,
                                  boolean disableDefaultStore,
                                  MyLoggingFacade loggingFacade, IdentifiableProviderImpl<User> userProvider) {
        super(cn, disableDefaultStore, loggingFacade, null, null);
        this.userProvider = userProvider;
    }


    UserStore defaultStore = null;
    @Override
    public UserStore getDefaultStore() {
        // make sure that if a memory store is being used, the same one is returned consistently or there will
        // be multiple, inconsistent copies.
        if(defaultStore == null){
              logger.info("Using default in memory user store.");
            defaultStore = new MemoryUserStore(userProvider);
        }
        return defaultStore;
    }

}
