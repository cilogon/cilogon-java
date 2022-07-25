package org.cilogon.oauth2.servlet.storage.idp;

import edu.uiuc.ncsa.myproxy.oa4mp.server.OA4MPConfigTags;
import edu.uiuc.ncsa.security.core.configuration.provider.MultiTypeProvider;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import org.apache.commons.configuration.tree.ConfigurationNode;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/19/12 at  6:42 PM
 */
public class MultiIDPStoreProvider extends MultiTypeProvider<IdentityProviderStore> implements OA4MPConfigTags {
    public MultiIDPStoreProvider(ConfigurationNode config,
                                 boolean disableDefaultStore, MyLoggingFacade loggingFacade) {
        super(config, disableDefaultStore, loggingFacade, null, IDENTITY_PROVIDERS);
    }

    MemoryIDPStore idpStore;
    @Override
    public IdentityProviderStore getDefaultStore() {
        if(idpStore == null){
            logger.info("Using default in memory IDP store.");
            idpStore =  new MemoryIDPStore(new IDPProvider());
        }
        return idpStore;
    }
}
