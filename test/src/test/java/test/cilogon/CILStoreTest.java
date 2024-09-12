package test.cilogon;


import org.oa4mp.server.test.StoreTest;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 5/4/12 at  2:25 PM
 */
public abstract class CILStoreTest extends StoreTest {
        public CILTestStoreProviderI2 getCILStoreTestProvider(){
            return (CILTestStoreProviderI2) getTSProvider();
        }
}
