package org.cilogon.oauth2.client;

import edu.uiuc.ncsa.security.core.cf.CFNode;
import edu.uiuc.ncsa.security.core.exceptions.MyConfigurationException;
import edu.uiuc.ncsa.security.core.util.ConfigurationLoader;
import org.oa4mp.client.loader.OA2ClientBootstrapper;

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
    public ConfigurationLoader getConfigurationLoader(CFNode node) throws MyConfigurationException {
        // so this prints out the CILogon client version mostly.
        return new CILOA2CFClientLoader(node);
    }
}
