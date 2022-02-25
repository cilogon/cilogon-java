package org.cilogon.qdl.module;

import edu.uiuc.ncsa.oa2.qdl.storage.QDLStoreAccessor;
import edu.uiuc.ncsa.oa2.qdl.storage.StoreFacade;
import org.cilogon.oauth2.servlet.loader.CILogonOA2ServiceEnvironment;
import org.cilogon.qdl.module.storage.TwoFactorMC;
import org.cilogon.qdl.module.storage.UserStemMC;

import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 12/22/20 at  1:36 PM
 */
/*
NOTE this does not extend PermissionStoreFacade since that  add in specific p-store methods
for getting clients etc. We don't want these in generic stores (e.g. the two factor store).
 */
public class CILStoreFacade extends StoreFacade {
    public static final String STORE_TYPE_USER_STORE = "user";
    public static final String STORE_TYPE_2FACTOR_STORE = "two_factor";

    @Override
    public List<String> getStoreTypes() {
        List<String> x = super.getStoreTypes();
        x.add(STORE_TYPE_2FACTOR_STORE);
        x.add(STORE_TYPE_USER_STORE);
        return x;
    }

    @Override
    protected QDLStoreAccessor createAccessor(String storeType) throws Exception {

        CILogonOA2ServiceEnvironment se = (CILogonOA2ServiceEnvironment) getEnvironment();
        QDLStoreAccessor accessor = null;
        switch (storeType) {
            case STORE_TYPE_USER_STORE:
                accessor = new QDLStoreAccessor(storeType, se.getUserStore(), se.getMyLogger());
                accessor.setMapConverter(new UserStemMC(se.getUserStore().getMapConverter()));
                break;
            case STORE_TYPE_2FACTOR_STORE:
                accessor = new QDLStoreAccessor(storeType, se.getTwoFactorStore(), se.getMyLogger());
                accessor.setMapConverter(new TwoFactorMC(se.getTwoFactorStore().getMapConverter()));
                break;
        }
        if (accessor == null) {
            accessor = super.createAccessor(storeType);
        }
        return accessor;
    }

}
