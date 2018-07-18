package test;

import org.cilogon.d2.DBServiceTests;
import org.cilogon.d2.DBServiceUserIDTests;
import org.cilogon.d2.ServiceTestSuite;
import org.cilogon.d2.impl.*;
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
       DBServiceTests.class,
        DBServiceUserIDTests.class,
        TwoFactorStoreTest.class,
        TokenTest.class,
        UserStoreTest.class,
        ArchivedUserStoreTest.class,
        IdentityProviderTest.class,
        NewCILTransactionStoreTest.class
})
public class CILServiceTestSuite2 extends ServiceTestSuite {
    @BeforeClass
    public static void initialize() {
        CILTestSuiteInitializer2 testSuiteInitializer;
        testSuiteInitializer = new CILTestSuiteInitializer2(new CILOA2Bootstrapper());
        testSuiteInitializer.init();
    }
}
