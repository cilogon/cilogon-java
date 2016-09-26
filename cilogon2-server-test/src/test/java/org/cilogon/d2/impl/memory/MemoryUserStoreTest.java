package org.cilogon.d2.impl.memory;

import edu.uiuc.ncsa.security.storage.MemoryStore;
import org.cilogon.d2.CILTestStoreProvider;
import org.cilogon.d2.ServiceTestUtils;
import org.cilogon.d2.impl.UserStoreTest;
import org.cilogon.d2.storage.InvalidUserIdException;
import org.cilogon.d2.storage.User;
import org.cilogon.d2.storage.impl.memorystore.MemoryUserStore;
import org.cilogon.d2.storage.provider.UserIdentifierProvider;
import org.cilogon.d2.storage.provider.UserProvider;
import org.cilogon.d2.util.Incrementable;
import org.junit.Test;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/14/12 at  11:02 AM
 */
public class MemoryUserStoreTest extends UserStoreTest {
    @Override
    public CILTestStoreProvider getTSProvider() {
        return (CILTestStoreProvider) ServiceTestUtils.getMemoryStoreProvider();
    }

    @Override
    protected Class getStoreClass() {
        return MemoryStore.class;
    }

    @Test
    public void testBadIncrementable() throws Exception {
        MemoryUserStore store = new MemoryUserStore(new UserProvider(new MyUIDProvider(), null));
        String r = getRandomString();

        // It has been saved as part of the registration process and is in the store.
        // Now create another one that is not. Here the user provider can only generate a single user id, mimicking the failure of
        // an SQL store or a file store to increment correctly.
        r = getRandomString();

        try {
          User  user = store.createAndRegisterUser(createRU("remote-" + r),
                    "idp:/" + r,
                    "idp-name-" + r,
                    "first-" + r,
                    "last-" + r,
                    "foo@bar." + r,
                    "affiliation" + r,
                    "displayName" + r,
                    "urn:ou:" + r);
            store.save(user);
            assert false : "Was able to create another user in the store with the same id.";
        } catch (InvalidUserIdException ix) {
            assert true;
        }
    }

    public class MyUIDProvider extends UserIdentifierProvider {
        public MyUIDProvider() {
            super(new BadIncrementable(), "fake:server");
        }
    }

    /**
     * A test class that doesnot increment right so we get bad identifiers from it.
     */
    public class BadIncrementable implements Incrementable {
        long onlyValue = 2L;

        @Override
        public boolean createNew(long initialValue) {
            return false;
        }

        @Override
        public long nextValue() {
            return onlyValue;
        }

        @Override
        public boolean destroy() {
            return false;
        }

        @Override
        public boolean init() {
            return false;
        }

        @Override
        public boolean createNew() {
            return false;
        }

        @Override
        public boolean isCreated() {
            return false;
        }

        @Override
        public boolean isInitialized() {
            return false;
        }

        @Override
        public boolean isDestroyed() {
            return false;
        }
    }
}
