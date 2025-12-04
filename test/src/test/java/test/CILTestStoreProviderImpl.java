package test;

import org.cilogon.oauth2.servlet.loader.CILOA2CFConfigurationLoader;
import org.oa4mp.server.api.storage.servlet.AbstractCFConfigurationLoader;
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
    CILOA2CFConfigurationLoader<OA2SE> loader;

    @Override
    public AbstractCFConfigurationLoader getConfigLoader() {
        if (loader == null) {
            loader = new CILOA2CFConfigurationLoader<>(findConfigNode(namedNode));
        }
        return loader;
    }
}
