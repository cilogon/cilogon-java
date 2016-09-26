package org.cilogon.oauth2.client;

import edu.uiuc.ncsa.myproxy.oa4mp.client.ClientEnvironment;
import edu.uiuc.ncsa.myproxy.oa4mp.client.OA4MPServiceProvider;
import edu.uiuc.ncsa.oa4mp.oauth2.client.OA2MPService;


/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/21/15 at  12:03 PM
 */
public class CILOA2MPService extends OA2MPService {
    public static class CILOA2MPProvider extends OA4MPServiceProvider{
        public CILOA2MPProvider(ClientEnvironment clientEnvironment) {
            super(clientEnvironment);
        }

        @Override
        public CILOA2MPService get() {
            return new CILOA2MPService(clientEnvironment);
        }
    }
    /**
     * Note that this constant is identical to the one in CILogonScopeHandler. Can't share this constant
     * between these modules though, so we repeat it here (and it is part of the spec any way, so it's not
     * apt to change).
     */
    public static String SCOPE_CILOGON_INFO = "org.cilogon.userinfo";

    public CILOA2MPService(ClientEnvironment environment) {
        super(environment);
    }

    @Override
    public String getRequestedScopes() {
        return super.getRequestedScopes() + " " + SCOPE_CILOGON_INFO;
    }
}
