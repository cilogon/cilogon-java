package org.cilogon.d2.util;

/**
 * This class sits in front of a DBSClient and if enabled, will try
 * to get/set information there.
 * <p>Created by Jeff Gaynor<br>
 * on 5/23/12 at  9:32 AM
 */
public class LegacyDBSFacade {
 /*   boolean enabled = false;
    DBServiceClient client;
    String host;
    String tokenKey;
    UserMapConverter converter;
    UserStore userStore;

    protected void init() {
        if (!enabled) return;
        client = new DBServiceClient(host, tokenKey);
    }

    public LegacyDBSFacade(boolean enabled,
                           String host,
                           String tokenKey,
                           UserStore userStore,
                           UserMapConverter converter) {
        this.host = host;
        this.enabled = enabled;
        this.tokenKey = tokenKey;
        this.converter = converter;
        this.userStore = userStore;
        init();
    }

    public User getUser(Identifier uid) {
        if (!enabled) return null;
        try {
            User user = userStore.create(false);
            converter.fromMap(client.getUser(uid), user);
            return user;
        } catch (UserNotFoundException x) {

        }
        return null;
    }

    public User getUser(String remoteUser, String idp) {
        if (!enabled) return null;
        try {
            User user = userStore.create(false);
            converter.fromMap(client.getUser(remoteUser, idp), user);
            return user;
        } catch (UserNotFoundException x) {
        }
        return null;
    }

    public User getUser(String remoteUser, String idp, String idpDisplayName, String firstName, String lastName, String email) {
        if (!enabled) return null;
        try {
            User user = userStore.create(false);
            converter.fromMap(client.getUser(remoteUser, idp, idpDisplayName, firstName, lastName, email), user);
            return user;
        } catch (UserNotFoundException x) {

        }
        return null;
    }

    public Identifier getUserId(String remoteUser, String idp) throws IOException {
        if (!enabled) return null;
        return client.getUserId(remoteUser, idp);
    }

    public XMLMap getPortalParameters(String token) {
        if (!enabled) return null;

        return client.getPortalParameters(token);
    }

    public User createUser(String remoteUser,
                           String idp,
                           String idpDisplayName,
                           String firstName,
                           String lastName,
                           String email) {
        if (!enabled) return null;
        try {
            User user = userStore.create(false);
            converter.fromMap(client.createUser(remoteUser, idp, idpDisplayName, firstName, lastName, email), user);
            return user;
        } catch (UserNotFoundException x) {

        }
        return null;
    }

    public boolean hasUser(String remoteUser, String idp){
        if(!enabled) return false;
        return client.hasUser(remoteUser, idp);
    }
    public boolean hasUser(Identifier uid){
        if(!enabled) return false;
        return client.hasUser(uid);
    }
*/
}
