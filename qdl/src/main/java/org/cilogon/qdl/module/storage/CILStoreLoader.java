package org.cilogon.qdl.module.storage;

import org.qdl_lang.extensions.QDLLoader;
import org.qdl_lang.module.Module;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/4/23 at  12:16 PM
 */
public class CILStoreLoader implements QDLLoader {
    @Override
    public List<Module> load() {
        List<Module> modules = new ArrayList<>();
        modules.add(new CILStoreModule().newInstance(null));
        return modules;
    }
}
