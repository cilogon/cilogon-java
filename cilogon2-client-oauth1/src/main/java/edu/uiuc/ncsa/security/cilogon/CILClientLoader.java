package edu.uiuc.ncsa.security.cilogon;

import edu.uiuc.ncsa.myproxy.oa4mp.client.ClientEnvironment;
import edu.uiuc.ncsa.myproxy.oa4mp.client.loader.ClientLoader;
import org.apache.commons.configuration.tree.ConfigurationNode;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 6/25/12 at  10:59 AM
 */
public class CILClientLoader<T extends ClientEnvironment> extends ClientLoader<T> {
    @Override
    public String getVersionString() {
        return "CILogon client configuration loader version " + VERSION_NUMBER;
    }


    public CILClientLoader(ConfigurationNode configurationNode) {
        super(configurationNode);
    }

}
