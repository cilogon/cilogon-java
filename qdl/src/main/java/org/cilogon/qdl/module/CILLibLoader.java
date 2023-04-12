package org.cilogon.qdl.module;

import edu.uiuc.ncsa.oa2.qdl.OA2LibLoader2;
import edu.uiuc.ncsa.qdl.state.State;
import edu.uiuc.ncsa.qdl.variables.QDLStem;
import org.cilogon.qdl.module.storage.CILStoreLoader;

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
