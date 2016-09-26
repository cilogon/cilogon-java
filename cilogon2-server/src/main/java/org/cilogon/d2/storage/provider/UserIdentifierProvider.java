package org.cilogon.d2.storage.provider;

import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import org.cilogon.d2.util.Incrementable;

import javax.inject.Provider;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/5/12 at  12:21 PM
 */
public class UserIdentifierProvider implements Provider<Identifier> {
    Incrementable incrementable;
    String server;

    public UserIdentifierProvider(Incrementable incrementable, String server) {
        this.incrementable = incrementable;
        if (server == null) {
            throw new IllegalArgumentException("Error: user identity provider must have a (URI) identifier.");
        }
        this.server = server;
        if (!this.server.endsWith("/")) {
            this.server = this.server + "/";
        }
    }

    public static final String USER_ID = "users";

    @Override
    public Identifier get() {
        return BasicIdentifier.newID(server + USER_ID + "/" + incrementable.nextValue());
    }
}
