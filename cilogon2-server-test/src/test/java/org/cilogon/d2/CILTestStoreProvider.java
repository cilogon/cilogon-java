package org.cilogon.d2;

import edu.uiuc.ncsa.myproxy.oa4mp.TestStoreProvider;
import edu.uiuc.ncsa.myproxy.oa4mp.server.servlet.AbstractConfigurationLoader;
import edu.uiuc.ncsa.security.delegation.token.TokenForge;
import edu.uiuc.ncsa.security.util.TestBase;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.cilogon.d2.storage.IdentityProvider;
import org.cilogon.d2.storage.IdentityProviderStore;
import org.cilogon.d2.storage.User;
import org.cilogon.d2.storage.UserStore;
import org.cilogon.d2.twofactor.TwoFactorStore;
import org.cilogon.d2.util.*;

import static edu.uiuc.ncsa.security.core.util.BasicIdentifier.newID;
import static org.cilogon.d2.RemoteDBServiceTest.createRU;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/13/12 at  4:00 PM
 */
public abstract class CILTestStoreProvider extends TestStoreProvider implements  CILTestStoreProviderI2 {

    SerialStrings serialStrings;

    protected ConfigurationNode node;
    protected AbstractConfigurationLoader loader;

    public CILogonSE getCILSE()  {
        return (CILogonSE) getSE();
    }

    @Override
    public TwoFactorStore getTwoFactorStore() throws Exception{
        return getCILSE().getTwoFactorStore();
    }

    @Override
    public UserStore getUserStore() throws Exception {
        return getCILSE().getUserStore();
    }

    @Override
    public IdentityProviderStore getIDP() throws Exception {
        return getCILSE().getIDPStore();
    }

    @Override
    public ArchivedUserStore getArchivedUserStore() throws Exception {
        return getCILSE().getArchivedUserStore();
    }



    @Override
    public Incrementable getSequence() throws Exception {
        return getCILSE().getIncrementable();
    }


    /*
   Use this to create a complete new user. Note that in tests where you are testing user creation, you should
   do it by hand. This call assumes that that is working.
    */

    @Override
    public User newUser(String firstName, String lastName) throws Exception {
        String rString = TestBase.getRandomString();
        IdentityProvider idp = new IdentityProvider(newID("urn:test/identity/provider/" + rString));
        getIDP().add(idp);
        User bob = getUserStore().createAndRegisterUser(createRU("remote-" + rString),
                idp.getIdentifierString(), "idp display name",
                firstName,
                lastName,
                firstName.toLowerCase() + "." + lastName.toLowerCase() + "@" + rString + ".edu",
                "student", firstName.toLowerCase() + " " + lastName.toLowerCase(), "urn:my:fake:university");
        return bob;
    }

    @Override
    public User newUser() throws Exception {
        return newUser("Muhammad", "Chang"); // most common two names on earth. Almost nobody has this name though...

    }

   @Override
   public TokenForge getTokenForge() {
        return getSE().getTokenForge();
    }

}
