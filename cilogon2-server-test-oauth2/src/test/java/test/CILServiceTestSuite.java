package test;

import org.cilogon.d2.RemoteDBServiceTest;
import org.cilogon.d2.ServiceTestSuite;
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
import org.cilogon.oauth2.servlet.loader.CILOA2Bootstrapper;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/18/17 at  4:15 PM
 */

@RunWith(Suite.class)
@Suite.SuiteClasses(value = {

        /* Uncomment these to test which of them you want/need. */
        RemoteDBServiceTest.class,
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
public class CILServiceTestSuite extends ServiceTestSuite {
    @BeforeClass
    public static void initialize() {
        CILTestSuiteInitializer testSuiteInitializer;
        testSuiteInitializer = new CILTestSuiteInitializer(new CILOA2Bootstrapper());
        testSuiteInitializer.init();
    }
}
