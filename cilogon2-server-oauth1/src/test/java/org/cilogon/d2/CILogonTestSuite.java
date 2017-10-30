package org.cilogon.d2;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/13/12 at  3:10 PM
 */

import edu.uiuc.ncsa.myproxy.oa4mp.NewCAStoreTest;
import edu.uiuc.ncsa.myproxy.oa4mp.NewClientStoreTest;
import edu.uiuc.ncsa.myproxy.oa4mp.NewTransactionTest;
import edu.uiuc.ncsa.myproxy.oa4mp.server.servlet.AbstractConfigurationLoader;
import junit.framework.TestSuite;
import org.cilogon.d2.impl.filesystem.FS2FStoreTest;
import org.cilogon.d2.impl.filesystem.FSArchivedUserStoreTest;
import org.cilogon.d2.impl.filesystem.FSIdentityProviderStoreTest;
import org.cilogon.d2.impl.filesystem.FSUserStoreTest;
import org.cilogon.d2.impl.memory.MemoryArchivedUserStoreTest;
import org.cilogon.d2.impl.memory.MemoryIDPTest;
import org.cilogon.d2.impl.memory.MemoryUserStoreTest;
import org.cilogon.d2.impl.mysql.*;
import org.cilogon.d2.impl.postgres.PG2FStoreTest;
import org.cilogon.d2.impl.postgres.PGIDPStoreTest;
import org.cilogon.d2.impl.postgres.PGUserStoreTest;
import org.cilogon.oauth1.loader.CILogonBootstrapper;
import org.cilogon.oauth1.loader.CILogonConfigurationLoader;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import static edu.uiuc.ncsa.myproxy.oa4mp.TestUtils.findConfigNode;


/**
 * <p>Created by Jeff Gaynor<br>
 * on Nov 27, 2010 at  1:28:14 PM
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
 //       DBServiceUserTests.class,
        DBServiceTests.class,
        DBServiceUserIDTests.class,
        FS2FStoreTest.class,
        MySQL2FStoreTest.class,
        PG2FStoreTest.class,
        CILTokenTest.class,
        NewClientStoreTest.class,
        NewCAStoreTest.class,
        NewTransactionTest.class,
        //FSClientTest.class,
        //FSCAStoreTest.class,
        //FSTransactionStoreTest.class,
        FSUserStoreTest.class,
        FSIdentityProviderStoreTest.class,
        FSArchivedUserStoreTest.class,
        //MClientStoreTest.class,
        //MCAStoreTest.class,
        //MemoryTransactionStoreTest.class,
        MemoryUserStoreTest.class,
        MemoryIDPTest.class,
        MemoryArchivedUserStoreTest.class,
        //PGClientStoreTest.class,
        //PGCAStoreTest.class,
        //PGTransactionStoreTest.class,
        PGUserStoreTest.class,
        PGIDPStoreTest.class,
        //MySQLClientStoreTest.class,
        //MySQLCAStoreTest.class,
        //PGArchivedUserStoreTest.class,
        MySQLTransactionStoreTest.class,
        MySQLArchivedUserStoreTest.class,
        MySQLUserStoreTest.class,
        MySQLIDPStoreTest.class

})
public class CILogonTestSuite extends TestSuite {


    protected static void setupMemoryTests() {
        ServiceTestUtils.setMemoryStoreProvider(new CILTestStoreProvider() {

            @Override
            public AbstractConfigurationLoader getConfigLoader() {
                if (loader == null) {
                    loader = new CILogonConfigurationLoader(findConfigNode("cilogon.cil2.memory"));
                }
                return loader;
            }
        });

    }

    protected static void setupFSTests() {
        ServiceTestUtils.setFsStoreProvider(new CILTestStoreProvider() {

            @Override
            public AbstractConfigurationLoader getConfigLoader() {
                if (loader == null) {
                    loader = new CILogonConfigurationLoader(findConfigNode("cilogon.cil2.fileStore"));
                }
                return loader;
            }
        });
    }

    protected static void setupPGTests() {
        ServiceTestUtils.setPgStoreProvider(new CILTestStoreProvider() {

            @Override
            public AbstractConfigurationLoader getConfigLoader() {
                if (loader == null) {
                    loader = new CILogonConfigurationLoader(findConfigNode("cilogon.cil2.postgres"));
                }
                return loader;
            }
        });
    }

    protected static void setupMySQLTests() {
        ServiceTestUtils.setMySQLStoreProvider(new CILTestStoreProvider() {

            @Override
            public AbstractConfigurationLoader getConfigLoader() {
                if (loader == null) {
                    loader = new CILogonConfigurationLoader(findConfigNode("cilogon.cil2.mysql"));
                }
                return loader;
            }
        });
    }

    @BeforeClass
    public static void initialize() {
        ServiceTestUtils.setBootstrapper(new CILogonBootstrapper());
        setupMemoryTests();
        setupFSTests();
        setupPGTests();
        setupMySQLTests();
    }
}
