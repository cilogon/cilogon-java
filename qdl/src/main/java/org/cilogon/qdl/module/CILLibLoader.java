package org.cilogon.qdl.module;

import org.cilogon.qdl.module.storage.CILStoreLoader;
import org.oa4mp.server.qdl.OA2LibLoader2;
import org.qdl_lang.state.State;
import org.qdl_lang.variables.QDLStem;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/4/23 at  12:17 PM
 */
public class CILLibLoader extends OA2LibLoader2 {
    protected String cilogonKey = "cilogon";
    @Override
    public void add(State state) {
        super.add(state);
        QDLStem x = new QDLStem();
        x.put("description", "The CILogons specific modules to access users and two factor stores");
        state.addLibEntries(cilogonKey, x);
        x.put("store", CILStoreLoader.class.getCanonicalName());
        state.addLibEntries(cilogonKey, x);
    }
}
