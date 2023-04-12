package org.cilogon.qdl.module.storage;

import edu.uiuc.ncsa.oa2.qdl.storage.PStoreAccessModule;
import edu.uiuc.ncsa.oa2.qdl.storage.StoreAccessModule;
import edu.uiuc.ncsa.qdl.extensions.QDLLoader;
import edu.uiuc.ncsa.qdl.module.Module;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 12/23/20 at  2:08 PM
 */
public class CILAccessLoader implements QDLLoader {
    @Override
    public List<Module> load() {
        ArrayList<Module> modules = new ArrayList<>();

        modules.add(new StoreAccessModule().newInstance(null));
        modules.add(new PStoreAccessModule().newInstance(null));
        modules.add(new CILStoreModule().newInstance(null));

        return modules;
    }
}
