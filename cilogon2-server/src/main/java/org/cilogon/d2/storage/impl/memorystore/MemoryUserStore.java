package org.cilogon.d2.storage.impl.memorystore;

import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.util.IdentifiableProviderImpl;
import edu.uiuc.ncsa.security.storage.MemoryStore;
import org.cilogon.d2.storage.*;
import org.cilogon.d2.storage.provider.UserProvider;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * NOTE: This does not retain the serial strings (or users) across system reboots. This is
 * at this point in time, a debugging tool.
 * <p>Created by Jeff Gaynor<br>
 * on 3/13/12 at  2:20 PM
 */
public class MemoryUserStore extends MemoryStore<User> implements UserStore {

    @Override
    public User createAndRegisterUser(UserMultiKey userMultiKey,
                                      String idP,
                                      String idPDisplayName,
                                      String firstName,
                                      String lastName,
                                      String email,
                                      String affiliation,
                                      String displayName,
                                      String organizationalUnit) {
        User user = create();
        user.setIdP(idP);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setIDPName(idPDisplayName);
        user.setUserMultiKey(userMultiKey);
        user.setAffiliation(affiliation);
        user.setDisplayName(displayName);
        user.setOrganizationalUnit(organizationalUnit);
        register(user);
        updateIndex(userMultiKey.getRemoteUserName(), idP, user, ruIndex);
        updateIndex(userMultiKey.getEptid(), idP, user, eptidIndex);
        updateIndex(userMultiKey.getEppn(), idP, user, eppnIndex);
        updateIndex(userMultiKey.getOpenID(), idP, user, openidIndex);
        updateIndex(userMultiKey.getOpenIDConnect(), idP, user, oidcIndex);
        return user;
    }

    protected void updateIndex(PersonName personName, String idp, User user, Map<String, User> index) {
        if (personName != null && !isEmpty(personName.getName())) {
            index.put(glom(personName.getName(), idp), user);
        }
    }

    boolean isEmpty(String x) {
        return x != null && x.length() == 0;
    }

    protected void fromIndex(PersonName personName, String idp, Map<String, User> index, Map<Identifier, User> users) {
        if (personName != null && !isEmpty(personName.getName())) {
            User user = index.get(glom(personName.getName(), idp));
            if (user != null) {
                users.put(user.getIdentifier(), user);
            }
        }
    }

    @Override
    public Collection<User> get(UserMultiKey userMultiKey, String idP) {
        HashMap<Identifier, User> users = new HashMap<>();
        fromIndex(userMultiKey.getRemoteUserName(), idP, ruIndex, users);
        fromIndex(userMultiKey.getEppn(), idP, eppnIndex, users);
        fromIndex(userMultiKey.getEptid(), idP, eptidIndex, users);
        fromIndex(userMultiKey.getOpenID(), idP, openidIndex, users);
        fromIndex(userMultiKey.getOpenIDConnect(), idP, oidcIndex, users);
        if (users.isEmpty()) throw new UserNotFoundException();
        return users.values();
    }

    @Override
    public Identifier getUserID(UserMultiKey userMultiKey, String idP) {
        Collection<User> users = get(userMultiKey, idP);
        if (users.size() == 1) return users.iterator().next().getIdentifier();
        throw new UserNotFoundException("Error: could not uniquely resolve user.");
    }

    public MemoryUserStore(IdentifiableProviderImpl<User> up) {
        super(up);
    }

    protected UserProvider getUserProvider() {
        return (UserProvider) identifiableProvider;
    }

    /*
   Internal note: This must clone users. The contract for the store is that instances of objects are
   immutable in the store, Simply caching them means that archiving operations are no longer atomic.
   Therefore, users are cloned on save and retrieval so that no modifications to objects are in the store.
   To change a stored user, save or update must be invoked.
    */
    @Override
    public User put(Identifier key, User value) {
        User u2 = null;
        u2 = (User) value.clone();
        return super.put(key, u2);
    }

    @Override
    public User create(boolean newIdentifier) {
        User user = getUserProvider().get(newIdentifier);
        if (newIdentifier && containsKey(user.getIdentifier())) {
            throw new InvalidUserIdException("Error: The id \"" + user.getIdentifierString() + "\" is already in use.");
        }
        return user;
    }

    @Override
    public User create() {
        return create(true);
    }

    @Override
    public void save(User value) {
        if (containsKey(value.getIdentifier())) {
            update(value);
        } else {
            realSave(value);
        }
    }

    static final String INFIX = "*"; // used to make unique key for remote user / idp lookup

    protected String glom(String remoteUser, String idp) {
        return remoteUser + INFIX + idp;
    }

    HashMap<String, User> ruIndex = new HashMap<String, User>();
    HashMap<String, User> eppnIndex = new HashMap<String, User>();
    HashMap<String, User> eptidIndex = new HashMap<String, User>();
    HashMap<String, User> openidIndex = new HashMap<String, User>();
    HashMap<String, User> oidcIndex = new HashMap<String, User>();

    @Override
    public User get(Object key) {
        User u = super.get(key);
        if (u == null) {
            throw new UserNotFoundException("User not found for identifier=\"" + key + "\"");
        }
        return u.clone();
    }

    @Override
    public void update(User user, boolean noNewSerialID) {
        if (!noNewSerialID) {
            Identifier serialString = getUserProvider().newIdentifier();
            if (containsKey(serialString)) {
                throw new InvalidUserIdException("Error: user id \"" + serialString + "\" already in use.");
            }
            user.setSerialIdentifier(serialString); // or subsequent calls have wrong serial string!

        }
        super.update(user);

    }

    @Override
    public void update(User value) {
        update(value, false);
    }
}
