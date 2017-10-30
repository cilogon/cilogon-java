package org.cilogon.d2;

import edu.uiuc.ncsa.myproxy.oa4mp.StoreTest;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 5/4/12 at  2:25 PM
 */
public abstract class CILStoreTest extends StoreTest {
        public CILTestStoreProviderI2 getCILStoreTestProvider(){
            return (CILTestStoreProviderI2) getTSProvider();
        }
}
