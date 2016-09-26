package org.cilogon.d2.storage.provider;

import edu.uiuc.ncsa.security.core.configuration.provider.MultiTypeProvider;
import edu.uiuc.ncsa.security.core.util.IdentifiableProviderImpl;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.cilogon.d2.storage.ArchivedUser;
import org.cilogon.d2.util.ArchivedUserStore;
import org.cilogon.d2.storage.impl.memorystore.MemoryArchivedUserStore;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/19/12 at  6:41 PM
 */
public class MultiArchivedUserStoreProvider extends MultiTypeProvider<ArchivedUserStore> {
    MultiUserStoreProvider userStore;
    IdentifiableProviderImpl<ArchivedUser> archivedUserProvider;

    public MultiArchivedUserStoreProvider(ConfigurationNode cn,
                                          boolean disableDefaultStore,
                                          MyLoggingFacade loggingFacade,
                                          MultiUserStoreProvider userStore,
                                          IdentifiableProviderImpl<ArchivedUser> archivedUserProvider
    ) {
        super(cn,disableDefaultStore, loggingFacade,  null, null);
        this.userStore = userStore;
        this.archivedUserProvider = archivedUserProvider;
    }


    ArchivedUserStore archivedUserStore;

    @Override
    public ArchivedUserStore getDefaultStore() {
        if (archivedUserStore == null) {
            logger.info("Using default in memory archived user store.");
            archivedUserStore = new MemoryArchivedUserStore(userStore.get(), archivedUserProvider);
        }
        return archivedUserStore;
    }
}

