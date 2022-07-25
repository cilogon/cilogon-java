package test;

import edu.uiuc.ncsa.security.util.TemplateTest;
import test.cilogon.DBServiceTests;
import test.cilogon.DBServiceUserIDTests;
import test.cilogon.DBServiceUserTests;
import test.cilogon.ServiceTestSuite;
import test.cilogon.impl.*;
import test.cilogon.util.TokenTest;
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
        // Start with DB service tests
        DNTests.class,
        DBServiceTests.class,
        DBServiceUserIDTests.class,
        DBServiceUserTests.class,
      // Other tests
        TemplateTest.class,
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
