package org.cilogon.oauth2.servlet.storage.twofactor;

import edu.uiuc.ncsa.myproxy.oa4mp.server.OA4MPConfigTags;
import edu.uiuc.ncsa.security.storage.FSProvider;
import edu.uiuc.ncsa.security.storage.data.MapConverter;
import org.apache.commons.configuration.tree.ConfigurationNode;

import java.io.File;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/18/12 at  12:18 PM
 */
public class TwoFactorFSProvider extends FSProvider<TwoFactorFS>  implements OA4MPConfigTags {

    TwoFactorInfoProvider twoFactorInfoProvider;
    public TwoFactorFSProvider(ConfigurationNode config, TwoFactorInfoProvider twoFactorInfoProvider, MapConverter converter) {
        super(config, FILE_STORE, TWO_FACTOR, converter);
        this.twoFactorInfoProvider = twoFactorInfoProvider;
    }

    @Override
    protected TwoFactorFS produce(File dataPath, File indexPath, boolean removeEmptyFiles) {
        return new TwoFactorFS(dataPath, indexPath, twoFactorInfoProvider, converter, removeEmptyFiles);
    }
}
