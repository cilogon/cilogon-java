package org.cilogon.oauth2.servlet.storage.user;

import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.util.IdentifiableProviderImpl;
import org.cilogon.oauth2.servlet.util.SerialStrings;

import java.util.Date;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/5/12 at  12:23 PM
 */
public class UserProvider extends IdentifiableProviderImpl<User> {
    public UserProvider(UserIdentifierProvider idProvider, SerialStrings serialStrings) {
        super(idProvider);
        this.serialStrings = serialStrings;
    }

    protected SerialStrings serialStrings;


    /**
     * This will make a new user object. If the argument is true, a new identifier will be made. If not,
     * the ideantifier will be null (e.g., ig you are populating the user object).
     *
     * @param newIdentifier
     * @return
     */
    public User get(boolean newIdentifier) {
        User u = new User(createNewId(newIdentifier), serialStrings);
        u.setCreationTS(new Date());
        return u;
    }

    public Identifier newIdentifier() {
        return createNewId(true);
    }
}
