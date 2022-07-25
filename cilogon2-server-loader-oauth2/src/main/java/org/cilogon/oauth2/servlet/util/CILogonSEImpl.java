package org.cilogon.oauth2.servlet.util;

import org.cilogon.oauth2.servlet.storage.idp.IdentityProviderStore;
import org.cilogon.oauth2.servlet.storage.user.UserStore;
import org.cilogon.oauth2.servlet.twofactor.TwoFactorStore;

import javax.inject.Provider;


/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/1/12 at  11:37 AM
 */
public class CILogonSEImpl implements CILogonSE {
    /**
     * This is used by the database service to specify the number of attempts
     * will be made to find a new, unused, userid. Generally if it takes too many attempts
     * that indicates that there is some other issue. If this number is exceeded, then an
     * exception is thrown. This is not set in a configuration file, just here.
     */
    private int maxUserIdRetries = 100;

    public CILogonSEImpl(Provider<UserStore> usp,
                         Provider<ArchivedUserStore> muap,
                         Provider<IdentityProviderStore> idp,
                         Provider<Incrementable> mip,
                         Provider<TwoFactorStore> m2p,
                         boolean computeFNAL
    ) {
        this.usp = usp;
        this.ausp = muap;
        this.idpsp = idp;
        this.ip = mip;
        this.m2p = m2p;
        this.computeFNAL = computeFNAL;
    }

    Provider<TwoFactorStore> m2p;
    TwoFactorStore twoFactorStore;

    @Override
    public TwoFactorStore getTwoFactorStore() {
        if (twoFactorStore == null) {
            twoFactorStore = m2p.get();
        }
        return twoFactorStore;
    }

    Provider<UserStore> usp;

    UserStore userStore;

    @Override
    public UserStore getUserStore() {
        if (userStore == null) {
            userStore = usp.get();
        }
        return userStore;
    }

    Provider<ArchivedUserStore> ausp;
    ArchivedUserStore archivedUserStore;

    @Override
    public ArchivedUserStore getArchivedUserStore() {
        if (archivedUserStore == null) {
            archivedUserStore = ausp.get();
        }
        return archivedUserStore;
    }

    Provider<IdentityProviderStore> idpsp;
    IdentityProviderStore identityProviderStore;

    @Override
    public IdentityProviderStore getIDPStore() {
        if (identityProviderStore == null) {
            identityProviderStore = idpsp.get();
        }
        return identityProviderStore;
    }


    Provider<Incrementable> ip;

    @Override
    public Incrementable getIncrementable() {
        if (incrementable == null) {
            incrementable = ip.get();
        }
        return incrementable;
    }

    Incrementable incrementable;

    public int getMaxUserIdRetries() {
        return maxUserIdRetries;
    }

    public void setMaxUserIdRetries(int maxUserIdRetries) {
        this.maxUserIdRetries = maxUserIdRetries;
    }
    boolean computeFNAL = false;
    @Override
    public boolean isComputeFNAL() {
        return computeFNAL;
    }

    @Override
    public boolean isPrintTSInDebug() {
        return false;
    }
}
