package org.cilogon.oauth2.servlet.loader;

import edu.uiuc.ncsa.myproxy.oa4mp.server.ServiceEnvironmentImpl;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.cilogon.d2.storage.User;
import org.cilogon.d2.storage.provider.UserIdentifierProvider;
import org.cilogon.d2.storage.provider.UserProvider;
import org.cilogon.d2.util.CILogonStoreLoader;
import org.cilogon.d2.util.SerialStrings;

import java.util.Date;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 11/11/15 at  4:33 PM
 */
public class OA2CILogonStoreLoader<T extends ServiceEnvironmentImpl> extends CILogonStoreLoader<T> {
    public OA2CILogonStoreLoader(ConfigurationNode node) {
        super(node);
    }

    @Override
    public UserProvider getUP() {
        if (userProvider == null) {
            userProvider = new CILU2Provider(new UserIdentifierProvider(getIp().get(), getTokenPrefixProvider().get()), getSsp().get());
        }
        return userProvider;

    }

    public static class CILU2Provider extends UserProvider {
        public CILU2Provider(UserIdentifierProvider idProvider, SerialStrings serialStrings) {
            super(idProvider, serialStrings);
        }

        @Override
        public User get(boolean newIdentifier) {
            User u = new User(createNewId(newIdentifier), serialStrings);
            u.setCreationTime(new Date());
            return u;
        }
    }
}
