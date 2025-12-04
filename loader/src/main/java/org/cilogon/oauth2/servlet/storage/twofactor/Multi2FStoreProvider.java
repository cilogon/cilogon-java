package org.cilogon.oauth2.servlet.storage.twofactor;

import edu.uiuc.ncsa.security.core.cf.CFNode;
import edu.uiuc.ncsa.security.core.configuration.provider.MultiTypeProvider;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import org.oa4mp.server.api.OA4MPConfigTags;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/18/12 at  11:04 AM
 */
public class Multi2FStoreProvider  extends MultiTypeProvider<TwoFactorStore> implements OA4MPConfigTags {
    public Multi2FStoreProvider(CFNode config, boolean disableDefaultStore, MyLoggingFacade logger) {
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
