package org.cilogon.d2;

import edu.uiuc.ncsa.myproxy.oa4mp.TestStoreProviderInterface;
import edu.uiuc.ncsa.security.core.util.ConfigurationLoader;
import edu.uiuc.ncsa.security.delegation.token.TokenForge;
import org.cilogon.d2.storage.IdentityProviderStore;
import org.cilogon.d2.storage.User;
import org.cilogon.d2.storage.UserStore;
import org.cilogon.d2.twofactor.TwoFactorStore;
import org.cilogon.d2.util.ArchivedUserStore;
import org.cilogon.d2.util.Incrementable;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/25/17 at  12:07 PM
 */
public interface CILTestStoreProviderI2 extends TestStoreProviderInterface{
    ConfigurationLoader getConfigLoader() ;

    TwoFactorStore getTwoFactorStore() throws Exception;

    UserStore getUserStore() throws Exception;

    IdentityProviderStore getIDP() throws Exception;

    ArchivedUserStore getArchivedUserStore() throws Exception;

    Incrementable getSequence() throws Exception;

    User newUser(String firstName, String lastName) throws Exception;

    User newUser() throws Exception;

    TokenForge getTokenForge();
}
