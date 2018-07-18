package org.cilogon.d2;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/13/12 at  3:10 PM
 */

import edu.uiuc.ncsa.myproxy.oa4mp.NewCAStoreTest;
import edu.uiuc.ncsa.myproxy.oa4mp.NewClientStoreTest;
import edu.uiuc.ncsa.myproxy.oa4mp.server.servlet.AbstractConfigurationLoader;
import junit.framework.TestSuite;
import org.cilogon.d2.impl.ArchivedUserStoreTest;
import org.cilogon.d2.impl.IdentityProviderTest;
import org.cilogon.d2.impl.NewCILTransactionStoreTest;
import org.cilogon.d2.impl.TwoFactorStoreTest;
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
        IdentityProviderTest.class,
        TwoFactorStoreTest.class,
        NewCILTransactionStoreTest.class,
        ArchivedUserStoreTest.class,
        CILTokenTest.class,
        NewClientStoreTest.class,
        NewCAStoreTest.class,

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
