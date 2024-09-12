package org.cilogon.oauth2.servlet.storage.user;

import edu.uiuc.ncsa.security.core.util.IdentifiableProviderImpl;
import edu.uiuc.ncsa.security.storage.FSProvider;
import edu.uiuc.ncsa.security.storage.data.MapConverter;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.cilogon.oauth2.servlet.util.Incrementable;
import org.oa4mp.server.api.OA4MPConfigTags;

import java.io.File;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/19/12 at  7:13 PM
 */
public class CILFSUserStoreProvider extends FSProvider<UserFileStore> implements OA4MPConfigTags {
    IdentifiableProviderImpl<User> userProvider;
    Incrementable incrementable;

    public CILFSUserStoreProvider(ConfigurationNode config,
                                  IdentifiableProviderImpl<User> userProvider,
                                  MapConverter converter,
                                  Incrementable incrementable
                                  ) {
        super(config, FILE_STORE, USERS, converter);
        this.userProvider = userProvider;
                                this.incrementable = incrementable;
    }

    @Override
    protected UserFileStore produce(File dataPath,
                                    File indexPath,
                                    boolean removeEmptyFiles,
                                    boolean removeFailedFiles) {
        UserFileStore u =  new UserFileStore(dataPath,indexPath,userProvider, converter, removeEmptyFiles, incrementable, removeFailedFiles);
        u.setUpkeepConfiguration(getUpkeepConfiguration());
        return u;
    }
}
