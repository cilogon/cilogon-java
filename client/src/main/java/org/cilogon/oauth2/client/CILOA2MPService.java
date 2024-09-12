package org.cilogon.oauth2.client;


import org.oa4mp.client.api.ClientEnvironment;
import org.oa4mp.client.loader.OA2MPService;
import org.oa4mp.client.loader.OA2MPServiceProvider;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/21/15 at  12:03 PM
 */
public class CILOA2MPService extends OA2MPService {
    public static class CILOA2MPProvider extends OA2MPServiceProvider {
        public CILOA2MPProvider(ClientEnvironment clientEnvironment) {
            super(clientEnvironment);
        }

        @Override
        public CILOA2MPService get() {
            return new CILOA2MPService(oa2ClientEnvironment);
        }
    }
 
    public CILOA2MPService(ClientEnvironment environment) {
        super(environment);
    }


}
