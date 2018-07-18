package org.cilogon.test;

import edu.uiuc.ncsa.myproxy.oa4mp.*;
import edu.uiuc.ncsa.security.delegation.storage.FileStoreTest;
import junit.framework.TestSuite;
import org.cilogon.d2.*;
import org.cilogon.d2.impl.ArchivedUserStoreTest;
import org.cilogon.d2.impl.TwoFactorStoreTest;
import org.cilogon.d2.impl.UserStoreTest;
import org.cilogon.oauth1.loader.CILogonBootstrapper;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 7/17/18 at  5:25 PM
 */

@RunWith(Suite.class)

@Suite.SuiteClasses({
        NewClientStoreTest.class,
        NewTransactionTest.class,
        FileStoreTest.class,
        UserStoreTest.class,
        TwoFactorStoreTest.class,
        ArchivedUserStoreTest.class,
        ServiceConfigTest.class,
        NewCAStoreTest.class,
        TokenTest.class,
        ServiceConfigTest.class,
        DBServiceTests.class,
        DBServiceUserIDTests.class
})
public class CILogonTestSuite extends TestSuite {
    @BeforeClass
      public static void initialize() {
        CILTestSuitInitializer tsi = new CILTestSuitInitializer(new CILogonBootstrapper());
        tsi.init();
    }
}
