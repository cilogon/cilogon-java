package org.cilogon.oauth2.servlet.util;


import edu.uiuc.ncsa.security.core.cf.CFNode;
import edu.uiuc.ncsa.security.storage.CFDBConfigLoader;
import edu.uiuc.ncsa.security.storage.sql.SQLStore;
import edu.uiuc.ncsa.security.storage.sql.mysql.MySQLConnectionPoolProvider;
import edu.uiuc.ncsa.security.storage.sql.postgres.PGConnectionPoolProvider;
import org.cilogon.oauth2.servlet.storage.TokenPrefixProvider;
import org.cilogon.oauth2.servlet.storage.archiveUser.*;
import org.cilogon.oauth2.servlet.storage.idp.*;
import org.cilogon.oauth2.servlet.storage.sequence.*;
import org.cilogon.oauth2.servlet.storage.twofactor.*;
import org.cilogon.oauth2.servlet.storage.user.*;
import org.oa4mp.server.api.OA4MPConfigTags;
import org.oa4mp.server.api.ServiceEnvironmentImpl;

import java.util.HashMap;

import static edu.uiuc.ncsa.security.core.configuration.StorageConfigurationTags.*;

/**
 * Stores specific to CILogon (as opposed to OA4MP generally).
 * <p>Created by Jeff Gaynor<br>
 * on 5/1/12 at  2:31 PM
 */
public  class CILogonCFStoreLoader<T extends ServiceEnvironmentImpl> extends CFDBConfigLoader<T> implements CILogonConfiguration{
    MultiUserStoreProvider usp;
    MultiIncrementableProvider ip;
    MultiArchivedUserStoreProvider muasp;
    MultiIDPStoreProvider midp;
    Multi2FStoreProvider m2p;
    protected UserProvider userProvider;
    ArchivedUserProvider archivedUserProvider;

    public CILogonCFStoreLoader(CFNode node) {
        super(node);
        String x = cn.getFirstAttribute(OA4MPConfigTags.DISABLE_DEFAULT_STORES);
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

    SerialStringProviderInterface ssp;

    public SerialStringProviderInterface getSsp() {
        if (ssp == null) {
            ssp = new CFSerialStringProvider(cn);
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
                     getDerbyConnectionPoolProvider(), DERBY_STORE, converter, get2fp()));
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
            ip.addListener(new DerbySequenceProvider(cn, getDerbyConnectionPoolProvider()));
        }
        return ip;
    }

    public MultiUserStoreProvider getUSP() {
        if (usp == null) {

            usp = new MultiUserStoreProvider(cn, isDefaultStoreDisabled(), loggerProvider.get(),
                    getUP(), getIp().get());
            UserConverter converter = new UserConverter(getUP());

            usp.addListener(new CILFSUserStoreProvider(cn, getUP(), converter, getIp().get()));
            usp.addListener(new CILSQLUserStoreProvider(cn,
                    getPgConnectionPoolProvider(),
                    POSTGRESQL_STORE, getUP(), converter,getIp().get()));
            usp.addListener(new CILSQLUserStoreProvider(cn,
                            getDerbyConnectionPoolProvider(),
                            DERBY_STORE, getUP(), converter,getIp().get()));
            usp.addListener(new CILSQLUserStoreProvider(cn,
                    getMySQLConnectionPoolProvider(),
                    MYSQL_STORE, getUP(), converter,getIp().get()));
            usp.addListener(new CILSQLUserStoreProvider(cn,
                    getMariaDBConnectionPoolProvider(),
                    MARIADB_STORE, getUP(), converter,getIp().get()));

        }
        return usp;
    }

    public MultiArchivedUserStoreProvider getMUASP() {
        if (muasp == null) {
            ArchivedUserConverter converter = new ArchivedUserConverter(new ArchivedUserKeys(), getAUP(), new UserConverter(getUP()));

            muasp = new MultiArchivedUserStoreProvider(cn, isDefaultStoreDisabled(), loggerProvider.get(), getUSP(), getAUP());
            muasp.addListener(new CILFSArchivedUserStoreProvider(cn, getUSP(), getAUP(), converter));
            UserStore userStore = getUSP().get();
            UserTable userTable = null;
            if(userStore instanceof SQLStore){
                 userTable = (UserTable) ((SQLStore)userStore).getTable();
                 // Since the user table can (and does in Derby) have its default name set
                // the actual configured current user table must be used.
            }
            muasp.addListener(new CILSQLArchivedUserStoreProvider(
                    cn,
                    getPgConnectionPoolProvider(),
                    POSTGRESQL_STORE,
                    getAUP(), converter,
                    userTable
            ));
            muasp.addListener(new CILSQLArchivedUserStoreProvider(
                      cn,
                      getDerbyConnectionPoolProvider(),
                      DERBY_STORE,
                      getAUP(), converter,
                    userTable
              ));
            muasp.addListener(new CILSQLArchivedUserStoreProvider(
                    cn,
                    getMySQLConnectionPoolProvider(),
                    MYSQL_STORE,
                    getAUP(), converter,
                    userTable
            ));
            muasp.addListener(new CILSQLArchivedUserStoreProvider(
                    cn,
                    getMariaDBConnectionPoolProvider(),
                    MARIADB_STORE,
                    getAUP(), converter,
                    userTable
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
            midp.addListener(new CILSQLIDPStoreProvider(cn, getDerbyConnectionPoolProvider(), DERBY_STORE, converter));
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
