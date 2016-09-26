package org.cilogon.d2.storage.provider;

import edu.uiuc.ncsa.myproxy.oa4mp.server.OA4MPConfigTags;
import edu.uiuc.ncsa.security.core.configuration.provider.CfgEvent;
import edu.uiuc.ncsa.security.core.configuration.provider.HierarchicalConfigProvider;
import edu.uiuc.ncsa.security.core.exceptions.MyConfigurationException;
import org.apache.commons.configuration.tree.ConfigurationNode;

import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/11/12 at  11:22 AM
 */
public class TokenPrefixProvider extends HierarchicalConfigProvider<String> implements OA4MPConfigTags {
    public TokenPrefixProvider(ConfigurationNode config) {
        super(config);
    }

    /**
     * For use in cases where you want/need to explictly set the server string. This
     * bypasses looking in the configuration.
     * @param tokenPrefixString
     */
    public TokenPrefixProvider(String tokenPrefixString) {
        this.tokenPrefixString = tokenPrefixString;
    }

    @Override
    protected boolean checkEvent(CfgEvent cfgEvent) {
        return false;
    }

    @Override
    public Object componentFound(CfgEvent configurationEvent) {
        if (checkEvent(configurationEvent)) {
            return get();
        }

        return null;
    }

    String tokenPrefixString;

    @Override
    public String get() {
        if (tokenPrefixString == null) {
            List kids = getConfig().getChildren(TOKEN_PREFIX);
            String v = null;
            if (!kids.isEmpty()) {
                ConfigurationNode sn = (ConfigurationNode) kids.get(0);
                tokenPrefixString = sn.getValue().toString();
            }
            if (tokenPrefixString == null) {
                throw new MyConfigurationException("Error: There is no token prefix string specified. Cannot create tokens.");
            }
            if(tokenPrefixString.endsWith("/")){
                tokenPrefixString = tokenPrefixString.substring(0, tokenPrefixString.length()-1); // make sure it terminates right
            }
        }
        return tokenPrefixString;
    }
}
