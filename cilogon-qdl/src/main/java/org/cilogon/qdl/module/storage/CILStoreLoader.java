package org.cilogon.qdl.module.storage;

import edu.uiuc.ncsa.qdl.extensions.QDLLoader;
import edu.uiuc.ncsa.qdl.module.Module;

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
