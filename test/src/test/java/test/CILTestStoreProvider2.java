package test;

import org.cilogon.oauth2.servlet.storage.archiveUser.ArchivedUserStore;
import org.cilogon.oauth2.servlet.storage.idp.IdentityProviderStore;
import org.cilogon.oauth2.servlet.storage.twofactor.TwoFactorStore;
import org.cilogon.oauth2.servlet.storage.user.User;
import org.cilogon.oauth2.servlet.storage.user.UserStore;
import org.cilogon.oauth2.servlet.util.Incrementable;
import org.oa4mp.server.api.storage.servlet.AbstractCFConfigurationLoader;
import org.oa4mp.server.test.TestStoreProvider2;
import test.cilogon.CILTestStoreProvider;
import test.cilogon.CILTestStoreProviderI2;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/24/17 at  11:30 AM
 */
public class CILTestStoreProvider2 extends TestStoreProvider2 implements CILTestStoreProviderI2 {
    public CILTestStoreProvider getCilTSP() {
        return cilTSP;
    }

    CILTestStoreProvider cilTSP ;

    public CILTestStoreProvider2(CILTestStoreProvider cilTSP) {
        this.cilTSP = cilTSP;
    }

    @Override
    public ArchivedUserStore getArchivedUserStore() throws Exception {
        return getCilTSP().getArchivedUserStore();
    }

    @Override
    public AbstractCFConfigurationLoader getConfigLoader() {
        return null;
    }

    @Override
    public TwoFactorStore getTwoFactorStore() throws Exception {
        return getCilTSP().getTwoFactorStore();
    }

    @Override
    public UserStore getUserStore() throws Exception {
        return getCilTSP().getUserStore();
    }

    @Override
    public IdentityProviderStore getIDP() throws Exception {
        return getCilTSP().getIDP();
    }

    @Override
    public Incrementable getSequence() throws Exception {
        return getCilTSP().getSequence();
    }

    @Override
    public User newUser(String firstName, String lastName) throws Exception {
        return getCilTSP().newUser(firstName,lastName);
    }

    @Override
    public User newUser() throws Exception {
        return getCilTSP().newUser();
    }
}
