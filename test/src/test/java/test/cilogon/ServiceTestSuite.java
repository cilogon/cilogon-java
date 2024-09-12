package test.cilogon;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/13/12 at  3:10 PM
 */

import junit.framework.TestSuite;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.oa4mp.server.test.NewCAStoreTest;
import test.cilogon.impl.TwoFactorStoreTest;
import test.cilogon.impl.UserStoreTest;
import test.cilogon.util.TokenTest;


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
        NewCAStoreTest.class,
        DBServiceUserTests.class,
        TwoFactorStoreTest.class,
        TokenTest.class,
        UserStoreTest.class,
})
public class ServiceTestSuite extends TestSuite {
    @BeforeClass
    public static void initialize() {
    }


}
