package org.cilogon.oauth2.servlet.loader;

import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.OA2ServiceTransaction;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.claims.BasicClaimsSourceImpl;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.loader.OA2ConfigurationLoader;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.storage.OA2SQLTransactionStoreProvider;
import edu.uiuc.ncsa.myproxy.oa4mp.server.admin.transactions.DSTransactionProvider;
import edu.uiuc.ncsa.myproxy.oa4mp.server.admin.transactions.OA4MPIdentifierProvider;
import edu.uiuc.ncsa.myproxy.oa4mp.server.storage.MultiDSClientStoreProvider;
import edu.uiuc.ncsa.security.core.IdentifiableProvider;
import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.configuration.Configurations;
import edu.uiuc.ncsa.security.core.configuration.provider.MultiTypeProvider;
import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.core.util.DebugUtil;
import edu.uiuc.ncsa.security.core.util.IdentifiableProviderImpl;
import edu.uiuc.ncsa.security.core.util.IdentifierProvider;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import edu.uiuc.ncsa.security.delegation.server.storage.ClientStore;
import edu.uiuc.ncsa.security.delegation.storage.Client;
import edu.uiuc.ncsa.security.delegation.storage.TransactionStore;
import edu.uiuc.ncsa.security.delegation.token.TokenForge;
import edu.uiuc.ncsa.security.oauth_2_0.server.claims.ClaimSource;
import edu.uiuc.ncsa.security.oauth_2_0.server.claims.ClaimSourceConfiguration;
import edu.uiuc.ncsa.security.storage.data.MapConverter;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPoolProvider;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.cilogon.d2.storage.ArchivedUser;
import org.cilogon.d2.storage.IdentityProviderStore;
import org.cilogon.d2.storage.User;
import org.cilogon.d2.storage.UserStore;
import org.cilogon.d2.storage.provider.TokenPrefixProvider;
import org.cilogon.d2.twofactor.TwoFactorInfo;
import org.cilogon.d2.twofactor.TwoFactorStore;
import org.cilogon.d2.util.*;
import org.cilogon.oauth2.servlet.storage.CILOA2ServiceTransaction;
import org.cilogon.oauth2.servlet.storage.CILOA2TransactionConverter;
import org.cilogon.oauth2.servlet.storage.CILOA2TransactionKeys;
import org.cilogon.oauth2.servlet.storage.CILOA2TransactionstoreProvider;

import javax.inject.Provider;

import static edu.uiuc.ncsa.myproxy.oa4mp.server.admin.transactions.OA4MPIdentifierProvider.TRANSACTION_ID;

/**
 * This handles the extensions to OA4MP and serves as a facade for the CILogon store loader.
 * <p>Created by Jeff Gaynor<br>
 * on 3/26/15 at  1:52 PM
 */
public class CILOA2ConfigurationLoader extends OA2ConfigurationLoader implements CILogonConfiguration {

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
                (TokenForge)getTokenForgeProvider().get(),
                (ClientStore<? extends Client >)getClientStoreProvider().get());
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
            CILogonOA2ServiceEnvironment se = new CILogonOA2ServiceEnvironment(
                    (MyLoggingFacade) loggerProvider.get(),
                    getTransactionStoreProvider(),
                    getClientStoreProvider(),
                    getMaxAllowedNewClientRequests(),
                    getRTLifetime(),
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
                    getMultiJSONStoreProvider(),
                    getCmConfigs(),
                    getQDLEnvironment(),
                    isRFC8693Enabled());
            return  se;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new GeneralException("Error: Could not create the runtime environment", e);
        }
    }

    @Override
    public ClaimSource getClaimSource() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        DebugUtil.trace(this, ".getClaimSource starting");
        if (claimSource == null) {
            ClaimSourceConfiguration claimSourceConfiguration = new ClaimSourceConfiguration();
            claimSourceConfiguration.setEnabled(false);
            claimSource= new BasicClaimsSourceImpl();
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
}
