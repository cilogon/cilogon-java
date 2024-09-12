package org.cilogon.oauth2.servlet.storage.idp;

import edu.uiuc.ncsa.security.storage.FSProvider;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.oa4mp.server.api.OA4MPConfigTags;

import java.io.File;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/19/12 at  8:10 PM
 */
public class CILFSIDPProvider extends FSProvider<IDPFileStore> implements OA4MPConfigTags {
    public CILFSIDPProvider(ConfigurationNode config, IDPConverter converter) {
        super(config, FILE_STORE, IDENTITY_PROVIDERS, converter);
    }


    @Override
    protected IDPFileStore produce(File dataPath,
                                   File indexPath,
                                   boolean removeEmptyFiles,
                                   boolean removeFailedFiles) {
        return new IDPFileStore(dataPath, indexPath, new IDPProvider(), converter, removeEmptyFiles, removeFailedFiles);
    }


}
