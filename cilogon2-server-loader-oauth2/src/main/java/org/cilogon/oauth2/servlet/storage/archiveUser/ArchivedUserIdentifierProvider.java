package org.cilogon.oauth2.servlet.storage.archiveUser;

import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.core.util.IdentifierProvider;

import java.net.URI;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/5/12 at  12:21 PM
 */
public class ArchivedUserIdentifierProvider extends IdentifierProvider<Identifier> {

    public ArchivedUserIdentifierProvider(String server) {
        super(URI.create(server), ARCHIVED_USER_ID, true);
    }

    @Override
    public Identifier get() {
        return BasicIdentifier.newID(uniqueIdentifier());
    }

    /**
     * This is used a the component part of the identifier.
     */
    public static final String ARCHIVED_USER_ID = "archivedUser";
}
