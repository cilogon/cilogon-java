package org.cilogon.oauth2.servlet.loader;

import edu.uiuc.ncsa.security.core.IdentifiableProvider;
import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.cf.CFNode;
import edu.uiuc.ncsa.security.core.configuration.provider.MultiTypeProvider;
import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.core.util.DebugUtil;
import edu.uiuc.ncsa.security.core.util.IdentifiableProviderImpl;
import edu.uiuc.ncsa.security.core.util.IdentifierProvider;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import edu.uiuc.ncsa.security.storage.data.MapConverter;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPoolProvider;
import org.apache.commons.codec.binary.Base64;
import org.cilogon.oauth2.servlet.storage.TokenPrefixProvider;
import org.cilogon.oauth2.servlet.storage.archiveUser.ArchivedUser;
import org.cilogon.oauth2.servlet.storage.archiveUser.ArchivedUserStore;
import org.cilogon.oauth2.servlet.storage.idp.IdentityProviderStore;
import org.cilogon.oauth2.servlet.storage.sequence.SerialStringProviderInterface;
import org.cilogon.oauth2.servlet.storage.transaction.CILOA2ServiceTransaction;
import org.cilogon.oauth2.servlet.storage.transaction.CILOA2TransactionConverter;
import org.cilogon.oauth2.servlet.storage.transaction.CILOA2TransactionKeys;
import org.cilogon.oauth2.servlet.storage.transaction.CILOA2TransactionstoreProvider;
import org.cilogon.oauth2.servlet.storage.twofactor.TwoFactorInfo;
import org.cilogon.oauth2.servlet.storage.twofactor.TwoFactorStore;
import org.cilogon.oauth2.servlet.storage.user.User;
import org.cilogon.oauth2.servlet.storage.user.UserStore;
import org.cilogon.oauth2.servlet.util.CILogonConfiguration;
import org.cilogon.oauth2.servlet.util.CILogonConstants;
import org.cilogon.oauth2.servlet.util.Incrementable;
import org.oa4mp.delegation.common.storage.TransactionStore;
import org.oa4mp.delegation.common.storage.clients.Client;
import org.oa4mp.delegation.common.token.TokenForge;
import org.oa4mp.delegation.server.server.claims.ClaimSource;
import org.oa4mp.delegation.server.server.claims.ClaimSourceConfiguration;
import org.oa4mp.delegation.server.storage.ClientStore;
import org.oa4mp.server.api.admin.transactions.DSTransactionProvider;
import org.oa4mp.server.api.admin.transactions.OA4MPIdentifierProvider;
import org.oa4mp.server.api.storage.MultiDSClientStoreProvider;
import org.oa4mp.server.loader.oauth2.OA2SE;
import org.oa4mp.server.loader.oauth2.claims.BasicClaimsSourceImpl;
import org.oa4mp.server.loader.oauth2.loader.OA2CFConfigurationLoader;
import org.oa4mp.server.loader.oauth2.storage.transactions.OA2SQLTransactionStoreProvider;
import org.oa4mp.server.loader.oauth2.storage.transactions.OA2ServiceTransaction;

import javax.inject.Provider;

import static org.oa4mp.server.api.admin.transactions.OA4MPIdentifierProvider.TRANSACTION_ID;

/**
 * This handles the extensions to OA4MP and serves as a facade for the CILogon store loader.
 * <p>Created by Jeff Gaynor<br>
 * on 3/26/15 at  1:52 PM
 */
public class CILOA2CFConfigurationLoader<C extends OA2SE> extends OA2CFConfigurationLoader implements CILogonConfiguration {

    CILogonConfiguration ciLogonConfiguration;

    public CILOA2CFConfigurationLoader(CFNode node) {
        this(node, null);
    }

    public CILOA2CFConfigurationLoader(CFNode node, MyLoggingFacade logger) {
        super(node, logger);
        ciLogonConfiguration = new OA2CILogonCFStoreLoader<>(node);

    }

    @Override
    public IdentifiableProviderImpl<TwoFactorInfo> get2fp() {
        return ciLogonConfiguration.get2fp();
    }

    @Override
    public SerialStringProviderInterface getSsp() {
        return ciLogonConfiguration.getSsp();
    }

    @Override
    public TokenPrefixProvider getTokenPrefixProvider() {
        return ciLogonConfiguration.getTokenPrefixProvider();
    }

    @Override
    public MultiTypeProvider<TwoFactorStore> getM2P() {
        return ciLogonConfiguration.getM2P();
    }

    @Override
    public MultiTypeProvider<Incrementable> getIp() {
        return ciLogonConfiguration.getIp();
    }

    @Override
    public MultiTypeProvider<UserStore> getUSP() {
        return ciLogonConfiguration.getUSP();
    }

    @Override
    public MultiTypeProvider<ArchivedUserStore> getMUASP() {
        return ciLogonConfiguration.getMUASP();
    }

    @Override
    public MultiTypeProvider<IdentityProviderStore> getMidp() {
        return ciLogonConfiguration.getMidp();
    }

    @Override
    public IdentifiableProviderImpl<ArchivedUser> getAUP() {
        return ciLogonConfiguration.getAUP();
    }

    @Override
    public IdentifiableProviderImpl<User> getUP() {
        return ciLogonConfiguration.getUP();
    }

    @Override
    public String getVersionString() {
        return "CILogon for OAuth2/OIDC server configuration loader, version " + VERSION_NUMBER;
    }

    public static class CILST2Provider extends DSTransactionProvider<OA2ServiceTransaction> {

        public CILST2Provider(IdentifierProvider<Identifier> idProvider) {
            super(idProvider);
        }

        @Override
        public CILOA2ServiceTransaction get(boolean createNewIdentifier) {
            return new CILOA2ServiceTransaction(createNewId(createNewIdentifier));
        }
    }

    @Override
    protected Provider<TransactionStore> getTSP() {
        IdentifiableProvider tp = new CILST2Provider(new OA4MPIdentifierProvider(TRANSACTION_ID, false));
        CILOA2TransactionKeys keys = new CILOA2TransactionKeys();
        CILOA2TransactionConverter<CILOA2ServiceTransaction> tc = new CILOA2TransactionConverter<>(keys,
                tp,
                (TokenForge) getTokenForgeProvider().get(),
                (ClientStore<? extends Client>) getClientStoreProvider().get());
        return getTSP(tp, tc);
    }

    @Override
    protected OA2SQLTransactionStoreProvider createSQLTSP(CFNode config, ConnectionPoolProvider cpp, String type, MultiDSClientStoreProvider clientStoreProvider, Provider tp, Provider tfp, MapConverter converter) {
        return new CILOA2TransactionstoreProvider(config, cpp, type, clientStoreProvider, tp, tfp, converter);
    }


    @Override
    public CILogonOA2ServiceEnvironment createInstance() {
        try {
            initialize();
            System.out.println(Base64.class.getProtectionDomain().getCodeSource().getLocation());

            CILogonOA2ServiceEnvironment se = new CILogonOA2ServiceEnvironment(
                    (MyLoggingFacade) loggerProvider.get(),
                    getTransactionStoreProvider(),
                    getTXStoreProvider(),
                    getVOStoreProvider(),
                    getClientStoreProvider(),
                    getMaxAllowedNewClientRequests(),
                    getAGLifetime(),
                    getMaxAGLifetime(),
                    getIDTokenLifetime(),
                    getMaxIDTokenLifetime(),
                    getMaxATLifetime(),
                    getATLifetime(),
                    getRTLifetime(),
                    getMaxRTLifetime(),
                    getClientApprovalStoreProvider(),
                    getMailUtilProvider(),
                    getMP(),
                    getAGIProvider(),
                    getATIProvider(),
                    getPAIProvider(),
                    getTokenForgeProvider(),
                    getConstants(),
                    getAuthorizationServletConfig(),
                    getUsernameTransformer(),
                    getPingable(),
                    getClientSecretLength(),
                    getUSP(),
                    getMUASP(),
                    getMidp(),
                    getIp(),
                    getM2P(),
                    getScopes(),
                    getClaimSource(),
                    getLdapConfiguration(),
                    isRefreshTokenEnabled(),
                    isTwoFactorSupportEnabled(),
                    getMaxClientRefreshTokenLifetime(),
                    isComputeFNAL(),
                    getMpp(),
                    getMacp(),
                    getJSONWebKeys(),
                    getIssuer(),
                    isUtilServerEnabled(),
                    isOIDCEnabled(),
                    getCmConfigs(),
                    getQDLEnvironment(),
                    isRFC8693Enabled(),
                    isQdlStrictACLS(),
                    isSafeGC(),
                    isCleanupLockingEnabled(),
                    getCleanupFailOnErrors(),
                    getRFC8628ServletConfig(),
                    isRFC8628Enabled(),
                    isprintTSInDebug(),
                    getCleanupInterval(),
                    getCleanupAlarms(),
                    isNotifyACEventEmailAddresses(),
                    isRFC7636Required(),
                    isDemoModeEnabled(),
                    getRTGracePeriod(),
                    isMonitorEnabled(),
                    getMonitorInterval(),
                    getMonitorAlarms(),
                    getDebugger(),
                    isCCFEnabled(),
                    getDISerivceConfig(),
                    isAllowPromptNone());
            return se;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new GeneralException("Error: Could not create the runtime environment", e);
        }
    }

    @Override
    public boolean isprintTSInDebug() {
        if (printTSInDebug == null) {
            try {
                printTSInDebug = Boolean.parseBoolean(cn.getFirstAttribute(PRINT_TS_IN_DEBUG));
            } catch (Throwable t) {
                // use default which is to doo safe garbage collection.
                // We let this be null to trigger pulling the value, if any, out of the
                // the configuration
                printTSInDebug = Boolean.FALSE; // ONLY CHANGE FOR CILOGON is to make the default false.
            }
            DebugUtil.trace(this, "print TS in debug? " + printTSInDebug);
        }
        return printTSInDebug;
    }

    @Override
    public ClaimSource getClaimSource() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        DebugUtil.trace(this, ".getClaimSource starting");
        if (claimSource == null) {
            ClaimSourceConfiguration claimSourceConfiguration = new ClaimSourceConfiguration();
            claimSourceConfiguration.setEnabled(false);
            claimSource = new BasicClaimsSourceImpl();
            claimSource.setConfiguration(claimSourceConfiguration);
        }
        return claimSource;
    }


    public boolean isComputeFNAL() {
        String x = cn.getFirstAttribute(CILogonConstants.COMPUTE_FNAL);
        boolean computeFNAL = false;
        if (x != null) {
            try {
                computeFNAL = Boolean.parseBoolean(x);
            } catch (Throwable t) {
                // do zilch. Just use default if not readable.
            }
        }
        return computeFNAL;
    }
 }
