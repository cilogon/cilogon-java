package org.cilogon.oauth2.servlet.storage.archiveUser;

import edu.uiuc.ncsa.security.core.util.IdentifiableProviderImpl;
import edu.uiuc.ncsa.security.storage.FSProvider;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.cilogon.oauth2.servlet.storage.user.MultiUserStoreProvider;
import org.oa4mp.server.api.OA4MPConfigTags;

import java.io.File;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/19/12 at  7:30 PM
 */
public class CILFSArchivedUserStoreProvider extends FSProvider<CILFSArchivedUserStore> implements OA4MPConfigTags {
    MultiUserStoreProvider usp;
    IdentifiableProviderImpl<ArchivedUser> archivedUserProvider;

    public CILFSArchivedUserStoreProvider(ConfigurationNode config,
                                          MultiUserStoreProvider userStoreProvider,
                                          IdentifiableProviderImpl<ArchivedUser> archivedUserProvider,
                                          ArchivedUserConverter archivedUserConverter) {
        super(config, FILE_STORE, ARCHIVED_USERS, archivedUserConverter);
        this.archivedUserProvider = archivedUserProvider;
        this.usp = userStoreProvider;
    }

    @Override

    protected CILFSArchivedUserStore produce(File dataPath,
                                             File indexPath,
                                             boolean removeEmptyFiles,
                                             boolean removeFailedFiles) {
        return new CILFSArchivedUserStore(dataPath,
                indexPath,
                usp.get(),
                archivedUserProvider, converter, removeEmptyFiles, removeFailedFiles);
    }
}
