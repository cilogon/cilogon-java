package org.cilogon.qdl.module;

import org.cilogon.qdl.module.storage.CILStoreLoader;
import org.oa4mp.server.qdl.OA2LibLoader2;
import org.qdl_lang.state.State;
import org.qdl_lang.variables.QDLStem;

import static org.qdl_lang.variables.StemUtility.put;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/4/23 at  12:17 PM
 */
public class CILLibLoader extends OA2LibLoader2 {
    public static String cilogonKey = "cilogon";
    @Override
    public void add(State state) {
        super.add(state);
        QDLStem x = new QDLStem();
        put(x,"description", "CILogon's store module adds access for users and two factor stores");
        state.addLibEntries(cilogonKey, x);
        put(x,"store", CILStoreLoader.class.getCanonicalName());
        state.addLibEntries(cilogonKey, x);
    }
}
