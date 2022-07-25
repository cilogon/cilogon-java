package test.cilogon;

import edu.uiuc.ncsa.myproxy.oa4mp.TestStoreProviderInterface;
import edu.uiuc.ncsa.security.core.util.ConfigurationLoader;
import edu.uiuc.ncsa.security.delegation.token.TokenForge;
import org.cilogon.oauth2.servlet.storage.idp.IdentityProviderStore;
import org.cilogon.oauth2.servlet.storage.user.User;
import org.cilogon.oauth2.servlet.storage.user.UserStore;
import org.cilogon.oauth2.servlet.twofactor.TwoFactorStore;
import org.cilogon.oauth2.servlet.util.ArchivedUserStore;
import org.cilogon.oauth2.servlet.util.Incrementable;

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
