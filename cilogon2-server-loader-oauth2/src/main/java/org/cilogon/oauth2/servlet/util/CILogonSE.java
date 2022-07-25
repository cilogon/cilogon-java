package org.cilogon.oauth2.servlet.util;

import org.cilogon.oauth2.servlet.storage.idp.IdentityProviderStore;
import org.cilogon.oauth2.servlet.storage.user.UserStore;
import org.cilogon.oauth2.servlet.twofactor.TwoFactorStore;

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

    boolean isPrintTSInDebug();
}
