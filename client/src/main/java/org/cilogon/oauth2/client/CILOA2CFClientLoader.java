package org.cilogon.oauth2.client;

import edu.uiuc.ncsa.security.core.cf.CFNode;
import org.oa4mp.client.api.OA4MPServiceProvider;
import org.oa4mp.client.loader.OA2CFClientLoader;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/2/15 at  2:01 PM
 */
public class CILOA2CFClientLoader extends OA2CFClientLoader {
    @Override
    public OA4MPServiceProvider getServiceProvider() {
        return new CILOA2MPService.CILOA2MPProvider(load());
    }

    public CILOA2CFClientLoader(CFNode node) {
        super(node);
    }

    @Override
    public String getVersionString() {
        return "CILogon OAuth2/OIDC client configuration loader version " + VERSION_NUMBER;
    }
}
