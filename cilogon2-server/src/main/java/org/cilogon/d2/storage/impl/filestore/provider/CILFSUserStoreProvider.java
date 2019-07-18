package org.cilogon.d2.storage.impl.filestore.provider;

import edu.uiuc.ncsa.myproxy.oa4mp.server.OA4MPConfigTags;
import edu.uiuc.ncsa.security.core.util.IdentifiableProviderImpl;
import edu.uiuc.ncsa.security.storage.FSProvider;
import edu.uiuc.ncsa.security.storage.data.MapConverter;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.cilogon.d2.storage.User;
import org.cilogon.d2.storage.impl.filestore.UserFileStore;

import java.io.File;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/19/12 at  7:13 PM
 */
public class CILFSUserStoreProvider extends FSProvider<UserFileStore> implements OA4MPConfigTags {
    IdentifiableProviderImpl<User> userProvider;

    public CILFSUserStoreProvider(ConfigurationNode config, IdentifiableProviderImpl<User> userProvider,
                                  MapConverter converter
                                  ) {
        super(config, FILE_STORE, USERS, converter);
        this.userProvider = userProvider;

    }

    @Override
    protected UserFileStore produce(File dataPath, File indexPath, boolean removeEmptyFiles) {
        return new UserFileStore(dataPath,indexPath,userProvider, converter, removeEmptyFiles);
    }
}
