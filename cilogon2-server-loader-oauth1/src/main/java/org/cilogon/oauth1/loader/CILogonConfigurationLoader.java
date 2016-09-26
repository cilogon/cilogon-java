package org.cilogon.oauth1.loader;

import edu.uiuc.ncsa.myproxy.oa4mp.loader.OA4MPConfigurationLoader;
import edu.uiuc.ncsa.myproxy.oa4mp.server.ServiceEnvironmentImpl;
import edu.uiuc.ncsa.myproxy.oa4mp.server.storage.MultiDSTransactionStoreProvider;
import edu.uiuc.ncsa.myproxy.oa4mp.server.storage.filestore.DSFSTransactionStoreProvider;
import edu.uiuc.ncsa.security.core.configuration.Configurations;
import edu.uiuc.ncsa.security.core.configuration.provider.MultiTypeProvider;
import edu.uiuc.ncsa.security.core.util.IdentifiableProviderImpl;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import edu.uiuc.ncsa.security.delegation.server.storage.ClientStore;
import edu.uiuc.ncsa.security.delegation.storage.Client;
import edu.uiuc.ncsa.security.delegation.storage.TransactionStore;
import edu.uiuc.ncsa.security.delegation.token.TokenForge;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.cilogon.d2.storage.ArchivedUser;
import org.cilogon.d2.storage.IdentityProviderStore;
import org.cilogon.d2.storage.User;
import org.cilogon.d2.storage.UserStore;
import org.cilogon.d2.storage.impl.postgres.provider.PGTransactionStoreProvider;
import org.cilogon.d2.storage.impl.sql.provider.CILSQLTransactionStoreProvider;
import org.cilogon.d2.storage.provider.CILTransactionProvider;
import org.cilogon.d2.storage.provider.TokenPrefixProvider;
import org.cilogon.d2.twofactor.TwoFactorInfo;
import org.cilogon.d2.twofactor.TwoFactorStore;
import org.cilogon.d2.util.*;

import javax.inject.Provider;

import static edu.uiuc.ncsa.myproxy.oa4mp.server.OA4MPConfigTags.MYSQL_STORE;
import static edu.uiuc.ncsa.myproxy.oa4mp.server.OA4MPConfigTags.POSTGRESQL_STORE;
import static edu.uiuc.ncsa.security.core.configuration.StorageConfigurationTags.MARIADB_STORE;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/19/12 at  6:14 PM
 */
public class CILogonConfigurationLoader<T extends ServiceEnvironmentImpl> extends OA4MPConfigurationLoader<T> implements CILogonConfiguration{
    /**
     * Practically we need multiple inheritance to extend the OA4MP config Loader and to get the
     * functionality of cilogon's user handling. Having an interface for the latter and doing
     * delegation is probably the most elegant solution.
     */
    CILogonConfiguration ciLogonConfiguration = null;
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
        return "CILogon 2 server configuration loader, version " + VERSION_NUMBER;
    }

    Provider<TokenForge> tokenForgeProvider;

    @Override
    public Provider<TokenForge> getTokenForgeProvider() {
        if (tokenForgeProvider == null) {
            tokenForgeProvider = new TokenForgeProvider(getTokenPrefixProvider());
        }
        return tokenForgeProvider;
    }

    public CILogonConfigurationLoader(ConfigurationNode node) {
        this(node, null);
    }

    public CILogonConfigurationLoader(ConfigurationNode node, MyLoggingFacade logger) {
        super(node, logger);
        ciLogonConfiguration = new CILogonStoreLoader(node);
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
    @Override
    public T createInstance() {
        CILogonServiceEnviroment se = new CILogonServiceEnviroment(
                loggerProvider.get(),
                getTSP(),
                getCSP(),
                getMaxAllowedNewClientRequests(),
                getCASP(),
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
                getUSP(),
                getMUASP(),
                getMidp(),
                getIp(),
                getM2P(),
                isComputeFNAL());
        return (T) se;
    }



    @Override
    protected Provider<TransactionStore> getTSP() {
        if (this.tsp == null) {
            CILTransactionProvider transactionProvider = new CILTransactionProvider();
            CILServiceTransactionConverter converter = new CILServiceTransactionConverter(transactionProvider,
                    getTokenForgeProvider().get(),
                    (ClientStore<? extends Client>) getCSP().get());
            MultiDSTransactionStoreProvider tsp = new MultiDSTransactionStoreProvider(cn, isDefaultStoreDisabled(), loggerProvider.get(), transactionProvider);
            tsp.addListener(new DSFSTransactionStoreProvider(cn, transactionProvider, getTokenForgeProvider(), converter));

            CILPGSTConverter pgconverter = new CILPGSTConverter(transactionProvider,
                    getTokenForgeProvider().get(),
                    (ClientStore<? extends Client>) getCSP().get());
            tsp.addListener(new PGTransactionStoreProvider(cn,
                    getPgConnectionPoolProvider(),
                    POSTGRESQL_STORE,
                    getTokenForgeProvider(),
                    transactionProvider, getCSP(),
                    pgconverter
            ));

            tsp.addListener(new CILSQLTransactionStoreProvider(cn,
                    getMySQLConnectionPoolProvider(),
                    MYSQL_STORE,
                    getTokenForgeProvider(),
                    transactionProvider, getCSP(),
                    converter
            ));
            tsp.addListener(new CILSQLTransactionStoreProvider(cn,
                              getMariaDBConnectionPoolProvider(),
                              MARIADB_STORE,
                              getTokenForgeProvider(),
                              transactionProvider, getCSP(),
                              converter
                      ));
            this.tsp = tsp;
        }
        return this.tsp;
    }

}
