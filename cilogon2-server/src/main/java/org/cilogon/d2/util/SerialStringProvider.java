package org.cilogon.d2.util;

import edu.uiuc.ncsa.myproxy.oa4mp.server.OA4MPConfigTags;
import edu.uiuc.ncsa.security.core.configuration.provider.CfgEvent;
import edu.uiuc.ncsa.security.core.configuration.provider.HierarchicalConfigProvider;
import edu.uiuc.ncsa.security.core.util.DoubleHashMap;
import org.apache.commons.configuration.tree.ConfigurationNode;

import java.net.URI;
import java.util.List;

import static edu.uiuc.ncsa.security.core.configuration.Configurations.getFirstAttribute;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/20/12 at  10:57 AM
 */
public class SerialStringProvider extends HierarchicalConfigProvider<SerialStrings> implements OA4MPConfigTags {
    ConfigurationNode configurationNode;

    public SerialStringProvider(ConfigurationNode cn) {
        configurationNode = cn;
    }

    @Override
    protected boolean checkEvent(CfgEvent cfgEvent) {
        return false;
    }


    @Override
    public SerialStrings get() {
        List kids = configurationNode.getChildren(SERIAL_STRINGS);
        DoubleHashMap<URI, String> sh = new DoubleHashMap<URI, String>();
        if (!kids.isEmpty()) {
            ConfigurationNode sn = (ConfigurationNode) kids.get(0);
            List list2 = sn.getChildren(SERIAL_STRING_TOKEN);
            for (Object obj : list2) {
                ConfigurationNode cn2 = (ConfigurationNode) obj;
                String tempNS = getFirstAttribute(cn2, SERIAL_STRING_NS);
                if(tempNS != null){
                    if(!tempNS.endsWith("/")){
                        tempNS = tempNS + "/";
                    }
                }
                sh.put(URI.create(tempNS), getFirstAttribute(cn2, SERIAL_STRING_PREFIX));
            }

        }
        return new SerialStrings(sh);
    }

    @Override
    public Object componentFound(CfgEvent configurationEvent) {
        return null;
    }
}
