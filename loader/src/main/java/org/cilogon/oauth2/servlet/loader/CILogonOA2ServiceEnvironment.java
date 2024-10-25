package org.cilogon.oauth2.servlet.loader;

import edu.uiuc.ncsa.security.core.Store;
import edu.uiuc.ncsa.security.core.util.MetaDebugUtil;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import edu.uiuc.ncsa.security.servlet.UsernameTransformer;
import edu.uiuc.ncsa.security.util.jwk.JSONWebKeys;
import edu.uiuc.ncsa.security.util.mail.MailUtilProvider;
import org.cilogon.oauth2.servlet.claims.UserClaimSource;
import org.cilogon.oauth2.servlet.servlet.DBServiceConfig;
import org.cilogon.oauth2.servlet.storage.archiveUser.ArchivedUserStore;
import org.cilogon.oauth2.servlet.storage.idp.IdentityProviderStore;
import org.cilogon.oauth2.servlet.storage.twofactor.TwoFactorStore;
import org.cilogon.oauth2.servlet.storage.user.UserStore;
import org.cilogon.oauth2.servlet.util.CILogonSE;
import org.cilogon.oauth2.servlet.util.CILogonSEImpl;
import org.cilogon.oauth2.servlet.util.Incrementable;
import org.oa4mp.delegation.common.storage.TransactionStore;
import org.oa4mp.delegation.common.token.TokenForge;
import org.oa4mp.delegation.server.issuers.AGIssuer;
import org.oa4mp.delegation.server.issuers.ATIssuer;
import org.oa4mp.delegation.server.issuers.PAIssuer;
import org.oa4mp.delegation.server.server.claims.ClaimSource;
import org.oa4mp.delegation.server.server.config.LDAPConfiguration;
import org.oa4mp.delegation.server.storage.ClientApprovalStore;
import org.oa4mp.delegation.server.storage.ClientStore;
import org.oa4mp.server.api.MyProxyFacadeProvider;
import org.oa4mp.server.api.ServiceEnvironmentImpl;
import org.oa4mp.server.api.admin.adminClient.AdminClientStore;
import org.oa4mp.server.api.admin.permissions.PermissionsStore;
import org.oa4mp.server.api.storage.servlet.AuthorizationServletConfig;
import org.oa4mp.server.loader.oauth2.OA2SE;
import org.oa4mp.server.loader.oauth2.cm.CMConfigs;
import org.oa4mp.server.loader.oauth2.servlet.RFC8628ServletConfig;
import org.oa4mp.server.loader.oauth2.storage.tx.TXStore;
import org.oa4mp.server.loader.oauth2.storage.vo.VOStore;
import org.oa4mp.server.loader.qdl.scripting.OA2QDLEnvironment;

import javax.inject.Provider;
import java.time.LocalTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/30/15 at  12:00 PM
 */
public class CILogonOA2ServiceEnvironment extends OA2SE implements CILogonSE {
    public CILogonOA2ServiceEnvironment(MyLoggingFacade logger,
                                        Provider<TransactionStore> tsp,
                                        Provider<TXStore> txStoreProvider,
                                        Provider<VOStore> voStoreProvider,
                                        Provider<ClientStore> csp,
                                        int maxAllowedNewClientRequests,
                                        long agLifetime,
                                        long maxAGLifetime,
                                        long idTokenLifetime,
                                        long maxIDTokenLifetime,
                                        long maxATLifetime,
                                        long atLifetime,
                                        long rtLifetime,
                                        long maxRTLifetime,
                                        Provider<ClientApprovalStore> casp,
                                        List<MyProxyFacadeProvider> mfp,
                                        MailUtilProvider mup,
                                        ServiceEnvironmentImpl.MessagesProvider messagesProvider,
                                        Provider<AGIssuer> agip,
                                        Provider<ATIssuer> atip,
                                        Provider<PAIssuer> paip,
                                        Provider<TokenForge> tfp,
                                        HashMap<String, String> constants,
                                        AuthorizationServletConfig ac,
                                        UsernameTransformer usernameTransformer,
                                        boolean isPingable,
                                        int clientSecretLength,
                                        Provider<UserStore> usp,
                                        Provider<ArchivedUserStore> ausp,
                                        Provider<IdentityProviderStore> idpsp,
                                        Provider<Incrementable> incp,
                                        Provider<TwoFactorStore> tfsp,
                                        Collection<String> scopes,
                                        ClaimSource claimSource,
                                        LDAPConfiguration ldapConfiguration,
                                        boolean isRefreshtokenEnabled,
                                        boolean isTwoFactorSupportEnabled,
                                        long maxClientRefreshTokenLifetime,
                                        boolean isComputeFNAL,
                                        Provider<PermissionsStore> permissionsStoreProvider,
                                        Provider<AdminClientStore> adminClientStoreProvider,
                                        JSONWebKeys jsonWebKeys,
                                        String issuer,
                                        boolean isUtilServletEnabled,
                                        boolean isOIDCEnabled,
                                        CMConfigs cmConfigs,
                                        OA2QDLEnvironment qe,
                                        boolean isRFC8693Enabled,
                                        boolean isqdlStrictAcls,
                                        boolean safeGC,
                                        boolean cleanupLockEnabled,
                                        boolean cleanupFailOnErrors,
                                        RFC8628ServletConfig rfc8628ServletConfig,
                                        boolean rfc8628Enabled,
                                        boolean isPrintTSInDebug,
                                        long cleanupInterval,
                                        Collection<LocalTime> cleanupAlarms,
                                        String notifyACEmailAddresses,
                                        boolean rfc7636Required,
                                        boolean isDemoMode,
                                        long rtGracePeriod,
                                        boolean isMonitorEnabled,
                                        long monitorInterval,
                                        Collection<LocalTime> monitorAlarms,
                                        MetaDebugUtil debugger,
                                        boolean ccfEnabled,
                                        DBServiceConfig dbServiceConfig
    ) {
        super(logger,
                tsp,
                txStoreProvider,
                voStoreProvider,
                csp,
                maxAllowedNewClientRequests,
                agLifetime,
                maxAGLifetime,
                idTokenLifetime,
                maxIDTokenLifetime,
                maxATLifetime,
                atLifetime,
                rtLifetime,
                maxRTLifetime,
                casp,
                mfp,
                mup,
                messagesProvider,
                agip,
                atip,
                paip,
                tfp,
                constants,
                ac,
                usernameTransformer,
                isPingable,
                permissionsStoreProvider,
                adminClientStoreProvider,
                clientSecretLength,
                scopes,
                claimSource,
                ldapConfiguration,
                isRefreshtokenEnabled,
                isTwoFactorSupportEnabled,
                maxClientRefreshTokenLifetime,
                jsonWebKeys,
                issuer,
                isUtilServletEnabled,
                isOIDCEnabled,
                cmConfigs,
                qe,
                isRFC8693Enabled,
                isqdlStrictAcls,
                safeGC,
                cleanupLockEnabled,
                cleanupFailOnErrors,
                rfc8628ServletConfig,
                rfc8628Enabled,
                isPrintTSInDebug,
                cleanupInterval,
                cleanupAlarms,
                notifyACEmailAddresses,
                rfc7636Required,
                isDemoMode,
                rtGracePeriod,
                isMonitorEnabled,
                monitorInterval,
                monitorAlarms,
                ccfEnabled,
                debugger);
        ciLogonSE = new CILogonSEImpl(usp, ausp, idpsp, incp, tfsp, isComputeFNAL);
        ciLogonSE.setDBServiceConfig(dbServiceConfig);
        this.dbServiceConfig = dbServiceConfig;
        if (claimSource instanceof UserClaimSource) {
            ((UserClaimSource) claimSource).setOa2SE(this);
        }
    }


    CILogonSE ciLogonSE;

    @Override
    public ArchivedUserStore getArchivedUserStore() {
        return ciLogonSE.getArchivedUserStore();
    }

    @Override
    public TwoFactorStore getTwoFactorStore() {
        return ciLogonSE.getTwoFactorStore();
    }

    @Override
    public UserStore getUserStore() {
        return ciLogonSE.getUserStore();
    }

    @Override
    public IdentityProviderStore getIDPStore() {
        return ciLogonSE.getIDPStore();
    }

    @Override
    public Incrementable getIncrementable() {
        return ciLogonSE.getIncrementable();
    }

    @Override
    public int getMaxUserIdRetries() {
        return ciLogonSE.getMaxUserIdRetries();
    }

    @Override
    public void setMaxUserIdRetries(int maxUserIdRetries) {
        ciLogonSE.setMaxUserIdRetries(maxUserIdRetries);
    }

    @Override
    public boolean isComputeFNAL() {
        return ciLogonSE.isComputeFNAL();
    }

    @Override
    public List<Store> listStores() {
        List<Store> stores = super.listStores();
        stores.add(getUserStore());
        stores.add(getIDPStore());
        stores.add(getArchivedUserStore());
        stores.add(getTwoFactorStore());
        return stores;
    }

    public DBServiceConfig getDBServiceConfig() {
        return dbServiceConfig;
    }

    public void setDBServiceConfig(DBServiceConfig dbServiceConfig) {
        this.dbServiceConfig = dbServiceConfig;
    }

    DBServiceConfig dbServiceConfig = null;

    @Override
    public List<Store> getAllStores() {
        if (storeList == null) {
            storeList = super.getAllStores();
            storeList.add(getUserStore());
            storeList.add(getIDPStore());
            storeList.add(getArchivedUserStore());
            storeList.add(getTwoFactorStore());
        }
        return storeList;
    }
}
