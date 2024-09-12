package test;

import org.cilogon.oauth2.servlet.loader.CILOA2ConfigurationLoader;
import org.oa4mp.server.api.storage.servlet.AbstractConfigurationLoader;
import org.oa4mp.server.loader.oauth2.OA2SE;
import test.cilogon.CILTestStoreProvider;

import static org.oa4mp.server.test.TestUtils.findConfigNode;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 7/18/18 at  12:46 PM
 */
public class CILTestStoreProviderImpl extends CILTestStoreProvider {
    public CILTestStoreProviderImpl(String namedNode) {
        this.namedNode = namedNode;
    }

    String namedNode;
    CILOA2ConfigurationLoader<OA2SE> loader;

    @Override
    public AbstractConfigurationLoader getConfigLoader() {
        if (loader == null) {
            loader = new CILOA2ConfigurationLoader<OA2SE>(findConfigNode(namedNode));
        }
        return loader;
    }
}
