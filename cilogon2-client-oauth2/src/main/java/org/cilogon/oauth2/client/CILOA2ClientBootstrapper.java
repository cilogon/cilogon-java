package org.cilogon.oauth2.client;

import edu.uiuc.ncsa.oa4mp.oauth2.client.OA2ClientBootstrapper;
import edu.uiuc.ncsa.security.core.exceptions.MyConfigurationException;
import edu.uiuc.ncsa.security.core.util.ConfigurationLoader;
import org.apache.commons.configuration.tree.ConfigurationNode;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/2/15 at  2:01 PM
 */
public class CILOA2ClientBootstrapper extends OA2ClientBootstrapper {
    public static final String CIL_OA2_CONFIG_FILE_KEY = "oa4mp:cil-oa2.client.config.file";
    public static final String CIL_OA2_CONFIG_NAME_KEY = "oa4mp:cil-oa2.client.config.name";

    @Override
    public String getOa4mpConfigFileKey() {
        return CIL_OA2_CONFIG_FILE_KEY;
    }

    @Override
    public String getOa4mpConfigNameKey() {
        return CIL_OA2_CONFIG_NAME_KEY;
    }

    @Override
    public ConfigurationLoader getConfigurationLoader(ConfigurationNode node) throws MyConfigurationException {
        // so this prints out the CILogon client version mostly.
        return new CILOA2ClientLoader(node);
    }
}
