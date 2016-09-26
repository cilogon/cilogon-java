package org.cilogon.d2.twofactor;

import edu.uiuc.ncsa.myproxy.oa4mp.server.OA4MPConfigTags;
import edu.uiuc.ncsa.security.core.configuration.provider.MultiTypeProvider;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import org.apache.commons.configuration.tree.ConfigurationNode;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/18/12 at  11:04 AM
 */
public class Multi2FStoreProvider  extends MultiTypeProvider<TwoFactorStore> implements OA4MPConfigTags {
    public Multi2FStoreProvider(ConfigurationNode config, boolean disableDefaultStore, MyLoggingFacade logger) {
        super(config, disableDefaultStore, logger, null, TWO_FACTOR);
    }

     TwoFactorMS twoFactorMS;

    @Override
    public TwoFactorStore getDefaultStore() {
        if(twoFactorMS == null){
            logger.info("Using default in memory two factor store.");
            twoFactorMS = new TwoFactorMS(new TwoFactorInfoProvider());

        }
        return twoFactorMS;
    }
}
