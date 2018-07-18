package test;

import edu.uiuc.ncsa.myproxy.oa4mp.server.servlet.AbstractConfigurationLoader;
import org.cilogon.d2.CILTestStoreProvider;
import org.cilogon.d2.CILTestStoreProviderI2;
import org.cilogon.d2.storage.IdentityProviderStore;
import org.cilogon.d2.storage.User;
import org.cilogon.d2.storage.UserStore;
import org.cilogon.d2.twofactor.TwoFactorStore;
import org.cilogon.d2.util.ArchivedUserStore;
import org.cilogon.d2.util.Incrementable;

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
    public AbstractConfigurationLoader getConfigLoader() {
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
