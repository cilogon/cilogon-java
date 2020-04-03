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
 
    public CILOA2MPService(ClientEnvironment environment) {
        super(environment);
    }


}
