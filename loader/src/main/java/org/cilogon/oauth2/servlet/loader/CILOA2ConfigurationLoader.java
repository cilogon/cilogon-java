package org.cilogon.oauth2.servlet.loader;

import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.OA2SE;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.claims.BasicClaimsSourceImpl;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.loader.OA2ConfigurationLoader;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.storage.transactions.OA2SQLTransactionStoreProvider;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.storage.transactions.OA2ServiceTransaction;
import edu.uiuc.ncsa.myproxy.oa4mp.server.admin.transactions.DSTransactionProvider;
import edu.uiuc.ncsa.myproxy.oa4mp.server.admin.transactions.OA4MPIdentifierProvider;
import edu.uiuc.ncsa.myproxy.oa4mp.server.storage.MultiDSClientStoreProvider;
import edu.uiuc.ncsa.oa4mp.delegation.common.storage.TransactionStore;
import edu.uiuc.ncsa.oa4mp.delegation.common.storage.clients.Client;
import edu.uiuc.ncsa.oa4mp.delegation.common.token.TokenForge;
import edu.uiuc.ncsa.oa4mp.delegation.oa2.server.claims.ClaimSource;
import edu.uiuc.ncsa.oa4mp.delegation.oa2.server.claims.ClaimSourceConfiguration;
import edu.uiuc.ncsa.oa4mp.delegation.server.storage.ClientStore;
import edu.uiuc.ncsa.security.core.IdentifiableProvider;
import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.configuration.Configurations;
import edu.uiuc.ncsa.security.core.configuration.provider.MultiTypeProvider;
import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.core.util.*;
import edu.uiuc.ncsa.security.storage.data.MapConverter;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPoolProvider;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.cilogon.oauth2.servlet.servlet.DBServiceConfig;
import org.cilogon.oauth2.servlet.storage.TokenPrefixProvider;
import org.cilogon.oauth2.servlet.storage.archiveUser.ArchivedUser;
import org.cilogon.oauth2.servlet.storage.archiveUser.ArchivedUserStore;
import org.cilogon.oauth2.servlet.storage.idp.IdentityProviderStore;
import org.cilogon.oauth2.servlet.storage.sequence.SerialStringProvider;
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

import javax.inject.Provider;
import java.util.List;

import static edu.uiuc.ncsa.myproxy.oa4mp.server.admin.transactions.OA4MPIdentifierProvider.TRANSACTION_ID;
import static edu.uiuc.ncsa.security.core.configuration.Configurations.getFirstAttribute;
import static edu.uiuc.ncsa.security.core.configuration.Configurations.getFirstNode;

/**
 * This handles the extensions to OA4MP and serves as a facade for the CILogon store loader.
 * <p>Created by Jeff Gaynor<br>
 * on 3/26/15 at  1:52 PM
 */
public class CILOA2ConfigurationLoader<C extends OA2SE> extends OA2ConfigurationLoader implements CILogonConfiguration {

    CILogonConfiguration ciLogonConfiguration;

    public CILOA2ConfigurationLoader(ConfigurationNode node) {
        this(node, null);
    }

    public CILOA2ConfigurationLoader(ConfigurationNode node, MyLoggingFacade logger) {
        super(node, logger);
        ciLogonConfiguration = new OA2CILogonStoreLoader<>(node);
    }

    @Override
    public IdentifiableProviderImpl<TwoFactorInfo> get2fp() {
        return ciLogonConfiguration.get2fp();
    }

    @Override
    public SerialStringProvider getSsp() {
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
    protected OA2SQLTransactionStoreProvider createSQLTSP(ConfigurationNode config, ConnectionPoolProvider cpp, String type, MultiDSClientStoreProvider clientStoreProvider, Provider tp, Provider tfp, MapConverter converter) {
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
                    getMaxRTLifetime(),
                    getClientApprovalStoreProvider(),
                    getMyProxyFacadeProvider(),
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
                    getDBSerivceConfig(),
                    getUucConfiguration());
            return se;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new GeneralException("Error: Could not create the runtime environment", e);
        }
    }

    @Override
    public boolean isprintTSInDebug() {
        if (printTSInDebug == null) {
            try {
                printTSInDebug = Boolean.parseBoolean(getFirstAttribute(cn, PRINT_TS_IN_DEBUG));
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
        String x = Configurations.getFirstAttribute(cn, CILogonConstants.COMPUTE_FNAL);
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

    /*
       protected OA2QDLEnvironment getQDLEnvironment() {
        List<ConfigurationNode> kids = cn.getChildren(QDLConfigurationConstants.CONFIG_TAG_NAME);
        ConfigurationNode node = null;
        if (kids.size() == 1) {
            node = kids.get(0);
            String x = getFirstAttribute(node, QDLConfigurationConstants.CONFG_ATTR_NAME);
            if (!getQdlConfigurationName().equals(x)) {
                DebugUtil.trace(this, "note that a default QDL configuration of " + getQdlConfigurationName() +
                        " was specified, but the actual name of the only configuration was \"" + "\", which was loaded.");
            }
        } else {
            // hunt for the default named node.
            for (ConfigurationNode tempNode : kids) {
                String x = getFirstAttribute(tempNode, QDLConfigurationConstants.CONFG_ATTR_NAME);
                if (getQdlConfigurationName().equals(x)) {
                    node = tempNode;
                    break;
                }
            }
        }
        if (node == null) {
            return new OA2QDLEnvironment();// no op. This is disabled.
        }
        // Note that the first argument is the name fo the file. In server mode this won't be available anyway
        // and is optional.
        String x = getFirstAttribute(node, STRICT_ACLS);
        if (!isTrivial(x)) {
            try {
                qdlStrictACLS = Boolean.parseBoolean(x);
            } catch (Throwable t) {
                // nothing to do.
            }
        }
        OA2QDLConfigurationLoader loader = new OA2QDLConfigurationLoader("(none)", node, loggerProvider.get());
        return (OA2QDLEnvironment) loader.load();
    }

     */
    DBServiceConfig dbServiceConfig = null;

    protected DBServiceConfig getDBSerivceConfig() {
        if (dbServiceConfig == null) {
            List<ConfigurationNode> kids = cn.getChildren(DBServiceConfig.DB_SERVICE_CONFIG_TAG);
            dbServiceConfig = new DBServiceConfig();
            dbServiceConfig.setEnabled(false); //default
            if (kids.isEmpty()) {
                return dbServiceConfig;
            }
            ConfigurationNode topNode = kids.get(0);
            String rawEnabled = getFirstAttribute(topNode, DBServiceConfig.DB_SERVICE_ENABLED_ATTRIBUTE);
            if (!StringUtils.isTrivial(rawEnabled)) {
                try {
                    dbServiceConfig.setEnabled(Boolean.parseBoolean(rawEnabled));
                } catch (Throwable t) {
                    info("Could not determine if db service is enabled: got \"" + rawEnabled + "\" in tag");
                }
            }
            ConfigurationNode usersNode = getFirstNode(topNode, DBServiceConfig.DB_SERVICE_USERS_TAG);
            List<ConfigurationNode> userNodes = usersNode.getChildren(DBServiceConfig.DB_SERVICE_USER_TAG);
            for (ConfigurationNode tempNode : userNodes) {
                String rawUser = getFirstAttribute(tempNode, DBServiceConfig.DB_SERVICE_NAME_ATTRIBUTE);
                String rawHash = getFirstAttribute(tempNode, DBServiceConfig.DB_SERVICE_HASH_ATTRIBUTE);
                dbServiceConfig.addUser(rawUser, rawHash);
            }
        }
        return dbServiceConfig;
    }
}
