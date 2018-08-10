package org.cilogon.d2.util;

import edu.uiuc.ncsa.myproxy.oa4mp.server.OA4MPConfigTags;
import edu.uiuc.ncsa.myproxy.oa4mp.server.ServiceEnvironmentImpl;
import edu.uiuc.ncsa.security.core.configuration.Configurations;
import edu.uiuc.ncsa.security.delegation.servlet.DBConfigLoader;
import edu.uiuc.ncsa.security.storage.sql.mysql.MySQLConnectionPoolProvider;
import edu.uiuc.ncsa.security.storage.sql.postgres.PGConnectionPoolProvider;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.cilogon.d2.storage.ArchivedUserKeys;
import org.cilogon.d2.storage.impl.filestore.provider.CILFSArchivedUserStoreProvider;
import org.cilogon.d2.storage.impl.filestore.provider.CILFSIDPProvider;
import org.cilogon.d2.storage.impl.filestore.provider.CILFSUserStoreProvider;
import org.cilogon.d2.storage.impl.filestore.provider.FSSequenceProvider;
import org.cilogon.d2.storage.impl.mariaDB.provider.MariaDBSequenceProvider;
import org.cilogon.d2.storage.impl.mysql.provider.MySQLSequenceProvider;
import org.cilogon.d2.storage.impl.postgres.provider.PGSequenceProvider;
import org.cilogon.d2.storage.impl.sql.provider.CILSQLArchivedUserStoreProvider;
import org.cilogon.d2.storage.impl.sql.provider.CILSQLIDPStoreProvider;
import org.cilogon.d2.storage.impl.sql.provider.CILSQLUserStoreProvider;
import org.cilogon.d2.storage.provider.*;
import org.cilogon.d2.twofactor.*;

import java.util.HashMap;

import static edu.uiuc.ncsa.myproxy.oa4mp.server.OA4MPConfigTags.MYSQL_STORE;
import static edu.uiuc.ncsa.myproxy.oa4mp.server.OA4MPConfigTags.POSTGRESQL_STORE;
import static edu.uiuc.ncsa.security.core.configuration.StorageConfigurationTags.MARIADB_STORE;

/**
 * Stores specific to CILogon (as opposed to OA4MP generally).
 * <p>Created by Jeff Gaynor<br>
 * on 5/1/12 at  2:31 PM
 */
public  class CILogonStoreLoader<T extends ServiceEnvironmentImpl> extends DBConfigLoader<T> implements CILogonConfiguration{
    MultiUserStoreProvider usp;
    MultiIncrementableProvider ip;
    MultiArchivedUserStoreProvider muasp;
    MultiIDPStoreProvider midp;
    Multi2FStoreProvider m2p;
    protected UserProvider userProvider;
    ArchivedUserProvider archivedUserProvider;

    public CILogonStoreLoader(ConfigurationNode node) {
        super(node);
        String x = Configurations.getFirstAttribute(cn, OA4MPConfigTags.DISABLE_DEFAULT_STORES);
        if (x != null) {
            isDefaultStoreDisabled(Boolean.parseBoolean(x));
        }

    }

    @Override
    public MySQLConnectionPoolProvider getMySQLConnectionPoolProvider() {
        return getMySQLConnectionPoolProvider("csd", "cilogon");
    }

    @Override
    public PGConnectionPoolProvider getPgConnectionPoolProvider() {
        return getPgConnectionPoolProvider("csd", "cilogon");
    }

    SerialStringProvider ssp;

    public SerialStringProvider getSsp() {
        if (ssp == null) {
            ssp = new SerialStringProvider(cn);
        }
        return ssp;
    }


    TokenPrefixProvider tokenPrefixProvider;

    public TokenPrefixProvider getTokenPrefixProvider() {
        if (tokenPrefixProvider == null) {
            tokenPrefixProvider = new TokenPrefixProvider(cn);
        }
        return tokenPrefixProvider;
    }

    TwoFactorInfoProvider twoFactorInfoProvider;

    public TwoFactorInfoProvider get2fp() {
        if (twoFactorInfoProvider == null) {
            twoFactorInfoProvider = new TwoFactorInfoProvider();
        }
        return twoFactorInfoProvider;
    }

    public Multi2FStoreProvider getM2P() {
        if (m2p == null) {
            m2p = new Multi2FStoreProvider(cn, isDefaultStoreDisabled(), loggerProvider.get());
            TwoFactorMapConverter converter = new TwoFactorMapConverter(get2fp());
            m2p.addListener(new TwoFactorFSProvider(cn, get2fp(), converter));
            m2p.addListener(new CILSQL2FStoreProvider(cn,
                    getPgConnectionPoolProvider(), POSTGRESQL_STORE, converter, get2fp()));
            m2p.addListener(new CILSQL2FStoreProvider(cn,
                    getMySQLConnectionPoolProvider(), MYSQL_STORE, converter, get2fp()));
            m2p.addListener(new CILSQL2FStoreProvider(cn,
                    getMySQLConnectionPoolProvider(), MARIADB_STORE, converter, get2fp()));

        }
        return m2p;
    }

    public MultiIncrementableProvider getIp() {
        if (ip == null) {
            ip = new MultiIncrementableProvider(cn, isDefaultStoreDisabled(), loggerProvider.get(), 0L);
            ip.addListener(new FSSequenceProvider(cn));
            ip.addListener(new PGSequenceProvider(cn, getPgConnectionPoolProvider()));
            ip.addListener(new MySQLSequenceProvider(cn, getMySQLConnectionPoolProvider()));
            ip.addListener(new MariaDBSequenceProvider(cn, getMariaDBConnectionPoolProvider()));
        }
        return ip;
    }

    public MultiUserStoreProvider getUSP() {
        if (usp == null) {
            usp = new MultiUserStoreProvider(cn, isDefaultStoreDisabled(), loggerProvider.get(), getUP());
            UserConverter converter = new UserConverter(getUP());

            usp.addListener(new CILFSUserStoreProvider(cn, getUP(), converter));
            usp.addListener(new CILSQLUserStoreProvider(cn,
                    getPgConnectionPoolProvider(),
                    POSTGRESQL_STORE, getUP(), converter));
            usp.addListener(new CILSQLUserStoreProvider(cn,
                    getMySQLConnectionPoolProvider(),
                    MYSQL_STORE, getUP(), converter));
            usp.addListener(new CILSQLUserStoreProvider(cn,
                    getMariaDBConnectionPoolProvider(),
                    MARIADB_STORE, getUP(), converter));

        }
        return usp;
    }

    public MultiArchivedUserStoreProvider getMUASP() {
        if (muasp == null) {
            ArchivedUserConverter converter = new ArchivedUserConverter(new ArchivedUserKeys(), getAUP(), new UserConverter(getUP()));

            muasp = new MultiArchivedUserStoreProvider(cn, isDefaultStoreDisabled(), loggerProvider.get(), getUSP(), getAUP());
            muasp.addListener(new CILFSArchivedUserStoreProvider(cn, getUSP(), getAUP(), converter));
            muasp.addListener(new CILSQLArchivedUserStoreProvider(
                    cn,
                    getPgConnectionPoolProvider(),
                    POSTGRESQL_STORE,
                    getAUP(), converter
            ));
            muasp.addListener(new CILSQLArchivedUserStoreProvider(
                    cn,
                    getMySQLConnectionPoolProvider(),
                    MYSQL_STORE,
                    getAUP(), converter
            ));
            muasp.addListener(new CILSQLArchivedUserStoreProvider(
                    cn,
                    getMariaDBConnectionPoolProvider(),
                    MARIADB_STORE,
                    getAUP(), converter
            ));
        }
        return muasp;
    }

    public MultiIDPStoreProvider getMidp() {
        if (midp == null) {
            IDPConverter converter = new IDPConverter(new IDPProvider());
            midp = new MultiIDPStoreProvider(cn, isDefaultStoreDisabled(), loggerProvider.get());
            midp.addListener(new CILFSIDPProvider(cn, converter));
            midp.addListener(new CILSQLIDPStoreProvider(cn, getPgConnectionPoolProvider(), POSTGRESQL_STORE, converter));
            midp.addListener(new CILSQLIDPStoreProvider(cn, getMySQLConnectionPoolProvider(), MYSQL_STORE, converter));
            midp.addListener(new CILSQLIDPStoreProvider(cn, getMariaDBConnectionPoolProvider(), MARIADB_STORE, converter));

        }
        return midp;
    }

    public UserProvider getUP() {
        if (userProvider == null) {
            userProvider = new UserProvider(new UserIdentifierProvider(getIp().get(), getTokenPrefixProvider().get()), getSsp().get());
        }
        return userProvider;

    }


    public ArchivedUserProvider getAUP() {
        if (archivedUserProvider == null) {
            archivedUserProvider = new ArchivedUserProvider(new ArchivedUserIdentifierProvider(getTokenPrefixProvider().get()));
        }
        return archivedUserProvider;
    }


    @Override
    public String getVersionString() {
        return "";
    }

    @Override
    public T load() {
        return null;
    }

    @Override
    public T createInstance() {
        return null;
    }

    @Override
    public HashMap<String, String> getConstants() {
        return null;
    }
}
