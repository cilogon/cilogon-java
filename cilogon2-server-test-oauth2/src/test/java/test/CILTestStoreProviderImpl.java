package test;

import edu.uiuc.ncsa.myproxy.oa4mp.server.servlet.AbstractConfigurationLoader;
import org.cilogon.d2.CILTestStoreProvider;
import org.cilogon.oauth2.servlet.loader.CILOA2ConfigurationLoader;

import static edu.uiuc.ncsa.myproxy.oa4mp.TestUtils.findConfigNode;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 7/18/18 at  12:46 PM
 */
public class CILTestStoreProviderImpl extends CILTestStoreProvider {
    public CILTestStoreProviderImpl(String namedNode) {
        this.namedNode = namedNode;
    }

    String namedNode;
    CILOA2ConfigurationLoader loader;

    @Override
    public AbstractConfigurationLoader getConfigLoader() {
        if (loader == null) {
            loader = new CILOA2ConfigurationLoader(findConfigNode(namedNode));
        }
        return loader;
    }
}
