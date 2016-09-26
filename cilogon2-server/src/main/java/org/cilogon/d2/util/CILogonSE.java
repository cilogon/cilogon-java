package org.cilogon.d2.util;

import org.cilogon.d2.storage.IdentityProviderStore;
import org.cilogon.d2.storage.UserStore;
import org.cilogon.d2.twofactor.TwoFactorStore;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/30/15 at  11:13 AM
 */
public interface CILogonSE {
    TwoFactorStore getTwoFactorStore();

    UserStore getUserStore();

    ArchivedUserStore getArchivedUserStore();

    IdentityProviderStore getIDPStore();

    Incrementable getIncrementable();

   int getMaxUserIdRetries();

    void setMaxUserIdRetries(int maxUserIdRetries);

    boolean isComputeFNAL();
}
