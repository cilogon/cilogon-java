package edu.uiuc.ncsa.security.cilogon;

import edu.uiuc.ncsa.myproxy.oa4mp.client.loader.ClientBootstrapper;
import edu.uiuc.ncsa.security.core.exceptions.MyConfigurationException;
import edu.uiuc.ncsa.security.core.util.ConfigurationLoader;
import org.apache.commons.configuration.tree.ConfigurationNode;

/**
 * Mostly this exists because these are intended for internal use and there will be many other clients of
 * various flavors installed. Having specific keys in the web.xml file cuts way down on configuration name collisions.
 * <p>Created by Jeff Gaynor<br>
 * on 6/25/12 at  11:02 AM
 */
public class CLIClientBootstrapper extends ClientBootstrapper {
    public static final String CIL_CONFIG_FILE_KEY = "oa4mp:cilogon.client.config.file";
    public static final String CIL_CONFIG_NAME_KEY = "oa4mp:cilogon.client.config.name";

    @Override
    public String getOa4mpConfigFileKey() {
        return CIL_CONFIG_FILE_KEY;
    }

    @Override
    public String getOa4mpConfigNameKey() {
        return CIL_CONFIG_NAME_KEY;
    }

    @Override
    public ConfigurationLoader getConfigurationLoader(ConfigurationNode node) throws MyConfigurationException {
        // so this prints out the CILogon client version mostly.
        return new CILClientLoader(node);
    }
}
