package org.cilogon.qdl.module.storage;

import org.oa4mp.server.qdl.storage.PStoreAccessModule;
import org.oa4mp.server.qdl.storage.StoreAccessModule;
import org.qdl_lang.extensions.QDLLoader;
import org.qdl_lang.module.Module;

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
