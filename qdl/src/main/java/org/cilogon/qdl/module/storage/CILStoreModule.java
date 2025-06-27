package org.cilogon.qdl.module.storage;

import org.cilogon.qdl.module.CILStoreFacade;
import org.oa4mp.server.qdl.storage.StoreAccessModule;
import org.oa4mp.server.qdl.storage.StoreFacade;
import org.qdl_lang.expressions.module.Module;
import org.qdl_lang.state.State;

import java.net.URI;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 12/23/20 at  1:42 PM
 */
public class CILStoreModule extends StoreAccessModule {
    public CILStoreModule() {
    }

    public CILStoreModule(URI namespace, String alias) {
        super(namespace, alias);
    }

    @Override
    public Module newInstance(State state) {
        CILStoreModule storeModule = new CILStoreModule(URI.create("cilogon:/qdl/store"), "store");
        storeModule.storeFacade = newStoreFacade();
        doIt(storeModule, state);
        if(state != null){
            storeModule.init(state);
        }
        setupModule(storeModule);
        return storeModule;
    }

    @Override
    public StoreFacade newStoreFacade() {
        return new CILStoreFacade();
    }
   /*
   E.g. on my test box
   users := j_load('cilogon.store');
   users#init('/home/ncsa/dev/csd/config/server-cil-oa2.xml', 'cilogon.oa2.mysql', $$STORE_TYPE.'user');
   print(users#read('http://cilogon.org/serverT/users/164123'));

        create_time : 1719423897000
       display_name : Tërrence d`Flëury
              email : terrencegf@gmail.com
         first_name : TERRENCEV33kwwZQ
                idp : http://random.com/login/oauth/authorize/V33kwwZQ
   idp_display_name : Random IDP open_id V33kwwZQ
   last_modified_ts : 1719423898000
          last_name : FLEURYV33kwwZQ
            open_id : V33kwwZQ:1719423897102
      serial_string : http://cilogon.org/serverT/users/164123
              state : {dn_state:7}
             us_idp : true
           user_uid : http://cilogon.org/serverT/users/164123

*/
}
