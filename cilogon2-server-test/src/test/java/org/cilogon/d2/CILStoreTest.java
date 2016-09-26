package org.cilogon.d2;

import edu.uiuc.ncsa.myproxy.oa4mp.StoreTest;
import edu.uiuc.ncsa.security.core.util.DateUtils;
import org.cilogon.d2.storage.User;

import java.sql.Timestamp;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 5/4/12 at  2:25 PM
 */
public abstract class CILStoreTest extends StoreTest {
        public CILTestStoreProvider getCILStoreTestProvider(){
            return (CILTestStoreProvider) getTSProvider();
        }




}
