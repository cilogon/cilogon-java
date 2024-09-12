package org.cilogon.oauth2.servlet.loader;

import org.apache.commons.configuration.tree.ConfigurationNode;
import org.cilogon.oauth2.servlet.storage.sequence.SerialStrings;
import org.cilogon.oauth2.servlet.storage.user.User;
import org.cilogon.oauth2.servlet.storage.user.UserIdentifierProvider;
import org.cilogon.oauth2.servlet.storage.user.UserProvider;
import org.cilogon.oauth2.servlet.util.CILogonStoreLoader;
import org.oa4mp.server.api.ServiceEnvironmentImpl;

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
            u.setCreationTS(new Date());
            return u;
        }
    }
}
