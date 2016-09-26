package org.cilogon.oauth1.loader;

import edu.uiuc.ncsa.myproxy.oa4mp.server.servlet.AbstractBootstrapper;
import edu.uiuc.ncsa.security.core.exceptions.MyConfigurationException;
import edu.uiuc.ncsa.security.core.util.ConfigurationLoader;
import edu.uiuc.ncsa.security.servlet.Initialization;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.cilogon.d2.servlet.CILServletInitializer;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/20/12 at  3:06 PM
 */
public class CILogonBootstrapper extends AbstractBootstrapper {
    public static final String CIL_CONFIG_FILE_KEY = "oa4mp:cilogon.server.config.file";
    public static final String CIL_CONFIG_NAME_KEY = "oa4mp:cilogon.server.config.name";

    @Override
    public String getOa4mpConfigNameKey() {
        return CIL_CONFIG_NAME_KEY;
    }

    @Override
    public String getOa4mpConfigFileKey() {
        return CIL_CONFIG_FILE_KEY;
    }

    @Override
    public ConfigurationLoader getConfigurationLoader(ConfigurationNode node) throws MyConfigurationException {
        return new CILogonConfigurationLoader(node);
    }

    @Override
    public Initialization getInitialization() {
        return new CILServletInitializer();
    }
}
