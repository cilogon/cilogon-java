package org.cilogon.d2;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/13/12 at  3:10 PM
 */

import junit.framework.TestSuite;
import org.cilogon.d2.impl.filesystem.*;
import org.cilogon.d2.impl.memory.MemoryArchivedUserStoreTest;
import org.cilogon.d2.impl.memory.MemoryIDPTest;
import org.cilogon.d2.impl.memory.MemoryTransactionStoreTest;
import org.cilogon.d2.impl.memory.MemoryUserStoreTest;
import org.cilogon.d2.impl.postgres.PGArchivedUserStoreTest;
import org.cilogon.d2.impl.postgres.PGIDPStoreTest;
import org.cilogon.d2.impl.postgres.PGTransactionStoreTest;
import org.cilogon.d2.impl.postgres.PGUserStoreTest;
import org.cilogon.d2.util.TokenTest;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


/**
 * Dummy template. You must setup your stores in the {@link #initialize()}
 * method and inject them into {@link ServiceTestUtils}. Note that
 * if you override and of the base test classes you should include
 * them in the list of classes to test. This suite by itself will not work
 * (because it sets up no stores).
 * It is just a template.
 * <p>Created by Jeff Gaynor<br>
 * on Nov 27, 2010 at  1:28:14 PM
 */
@RunWith(Suite.class)
@Suite.SuiteClasses(value = {

        /* Uncomment these to test which of them you want/need. */
        //RemoteDBServiceTest.class,
        DBServiceUserTests.class,
        FS2FStoreTest.class,
        TokenTest.class,
        FSTransactionStoreTest.class,
        FSUserStoreTest.class,
        FSIdentityProviderStoreTest.class,
        FSArchivedUserStoreTest.class,
        MemoryTransactionStoreTest.class,
        MemoryUserStoreTest.class,
        MemoryIDPTest.class,
        MemoryArchivedUserStoreTest.class,
        PGTransactionStoreTest.class,
        PGUserStoreTest.class,
        PGIDPStoreTest.class,
        PGArchivedUserStoreTest.class
})
public class ServiceTestSuite extends TestSuite {
    @BeforeClass
    public static void initialize() {
  /*      ServiceTestUtils.setBootstrapper(getBootstrapper());
        ServiceTestUtils.setMemoryStoreProvider(getTSP(getMemoryStoreConfigName()));
        //ServiceTestStoreProvider2 fsp = getTSP(getFileStoreConfigName()); // use this later to get its client converter. Any store would do.
        ServiceTestUtils.setFsStoreProvider(fsp);
        ServiceTestUtils.setMySQLStoreProvider(getTSP(getMySQLStoreConfigName()));
        ServiceTestUtils.setPgStoreProvider(getTSP(getPostgresStoreConfigName()));
*/
    }

/*    @Override
    public CILTestStoreProvider getTSP(final String namedNode) {
        return new TestStoreProvider() {
            CILogonConfigurationLoader loader;

            @Override
            public ConfigurationLoader<? extends ServiceEnvironment> getConfigLoader() {
                if (loader == null) {
                    //loader = new OA2ConfigurationLoader(findConfigNode(namedNode));
                    loader = new COLoader(findConfigNode(namedNode));
                }
                return loader;
            }

        };

    }*/
}
