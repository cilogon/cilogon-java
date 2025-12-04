package org.cilogon.oauth2.servlet.storage.sequence;

import edu.uiuc.ncsa.security.core.cf.CFNode;
import edu.uiuc.ncsa.security.core.configuration.provider.CfgEvent;
import edu.uiuc.ncsa.security.core.configuration.provider.HierarchicalConfigProvider;
import edu.uiuc.ncsa.security.core.util.DoubleHashMap;

import java.net.URI;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/20/12 at  10:57 AM
 */
public class CFSerialStringProvider<T extends SerialStrings> extends HierarchicalConfigProvider implements SerialStringProviderInterface {

    public CFSerialStringProvider(CFNode cn) {
        super(cn);
    }

    @Override
    protected boolean checkEvent(CfgEvent cfgEvent) {
        return false;
    }


    @Override
    public T get() {
        List<CFNode> kids = getCFNode().getChildren(SERIAL_STRINGS);
        DoubleHashMap<URI, String> sh = new DoubleHashMap<URI, String>();
        if (!kids.isEmpty()) {
            CFNode sn =  kids.get(0);
            List<CFNode> list2 = sn.getChildren(SERIAL_STRING_TOKEN);
            for (CFNode cn2 : list2) {
                String tempNS = cn2.getFirstAttribute( SERIAL_STRING_NS);
                if(tempNS != null){
                    if(!tempNS.endsWith("/")){
                        tempNS = tempNS + "/";
                    }
                }
                sh.put(URI.create(tempNS), cn2.getFirstAttribute(SERIAL_STRING_PREFIX));
            }

        }
        return (T) new SerialStrings(sh);
    }

    @Override
    public Object componentFound(CfgEvent configurationEvent) {
        return null;
    }
}
