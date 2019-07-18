package org.cilogon.d2.storage.impl.filestore;

import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.core.exceptions.NFWException;
import edu.uiuc.ncsa.security.core.exceptions.NotImplementedException;
import edu.uiuc.ncsa.security.core.util.IdentifiableProviderImpl;
import edu.uiuc.ncsa.security.storage.FileStore;
import edu.uiuc.ncsa.security.storage.data.MapConverter;
import org.cilogon.d2.storage.*;
import org.cilogon.d2.storage.provider.UserProvider;
import org.cilogon.d2.util.UserKeys;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/2/12 at  12:52 PM
 */
public class UserFileStore extends FileStore<User> implements UserStore {

    public UserFileStore(File file, UserProvider up, MapConverter converter, boolean removeEmptyFiles) {
        super(file, up, converter, removeEmptyFiles);
    }

    protected UserProvider getUP() {
        return (UserProvider) identifiableProvider;
    }

    static final String INFIX_STRING = "&";

    public UserFileStore(File storeDirectory,
                         File indexDirectory,
                         IdentifiableProviderImpl<User> up,
                         MapConverter converter,
                         boolean removeEmptyFiles) {
        super(storeDirectory, indexDirectory, up, converter, removeEmptyFiles);
    }

    @Override
    public void save(User t) {
        if (containsKey(t.getIdentifierString())) {
            update(t);
        } else {
            realSave(false, t);
        }
    }

    @Override
    public void realSave(boolean checkExists, User t) {
        super.realSave(checkExists, t);
        if (t.getUserMultiKey() != null) {
            Iterator<PersonName> it = t.getUserMultiKey().iterator();
            while (it.hasNext()) {
                PersonName personName = it.next();
                if (personName != null && t.getIdP() != null) {
                    String newID = toIdentifier(personName, t.getIdP());
                    try {
                        createIndexEntry(newID, t.getIdentifierString());
                    } catch (IOException e) {
                        throw new GeneralException("Error serializing item " + t + "to file ");
                    }
                }
            }
        }
    }

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
        User user = create(true);
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
        return user;
    }

    UserKeys uk() {
        return (UserKeys) converter.keys;
    }

    protected String toIdentifier(EduPersonPrincipleName eppn, String idp) {
        String x = uk().eppn() + ":" + eppn.getName() + INFIX_STRING + idp;
        return x;
    }

    /*
    This does not qualify the name, since we need to have backwards compatibility.
     */
    protected String toIdentifier(RemoteUserName remoteUser, String idp) {
        String x = remoteUser.getName() + INFIX_STRING + idp;
        return x;
    }

    protected String toIdentifier(PersonName remoteUser, String idp) {
        // Fugly. Thanks Java...
        if (remoteUser instanceof RemoteUserName) return toIdentifier((RemoteUserName) remoteUser, idp);
        if (remoteUser instanceof EduPersonPrincipleName) return toIdentifier((EduPersonPrincipleName) remoteUser, idp);
        if (remoteUser instanceof EduPersonTargetedID) return toIdentifier((EduPersonTargetedID) remoteUser, idp);
        if (remoteUser instanceof OpenID) return toIdentifier((OpenID) remoteUser, idp);
        if (remoteUser instanceof OpenIDConnect) return toIdentifier((OpenIDConnect) remoteUser, idp);
        throw new NFWException("Error: This method must exist for java to compile, but if it is ever called, then something is wrong with Java's class inheritance resolution mechanism.");
    }

    protected String toIdentifier(EduPersonTargetedID eptid, String idp) {
        String x = uk().eptid() + ":" + eptid.getName() + INFIX_STRING + idp;
        return x;
    }

    protected String toIdentifier(OpenID openID, String idp) {
        String x = uk().openID() + ":" + openID.getName() + INFIX_STRING + idp;
        return x;
    }

    protected String toIdentifier(OpenIDConnect openIDConnect, String idp) {
        String x = uk().oidc() + ":" + openIDConnect.getName() + INFIX_STRING + idp;
        return x;
    }

    protected String toIdentifier(String remoteUser, String idp) {
        String x = remoteUser + INFIX_STRING + idp;
        return x;
    }

    @Override
    public Collection<User> get(UserMultiKey userMultiKey, String idP) {
        HashMap<Identifier, User> users = new HashMap<>();
        Iterator<PersonName> it = userMultiKey.iterator();
        while (it.hasNext()) {
            PersonName personName = it.next();
            if (personName != null) {
                User u = loadFromIndex(hashString(toIdentifier(personName, idP)));
                if (u != null) {
                    users.put(u.getIdentifier(), u);
                }
            }
        }
        if (users.isEmpty()) {
            // contract says throw a user not found exception
            throw new UserNotFoundException("Error: no user for \"" + userMultiKey.toString() + "\", and idp=\"" + idP + "\"");
        }
        return users.values();
    }


    @Override
    public User get(Object key) {
        User user = null;
        try {
            user = super.get(key);
        }catch(Throwable t){
            // there was some other error getting the user
            throw new UserNotFoundException("User not found for identifier=\"" + key + "\"");
        }
        if (user != null) return user;
        throw new UserNotFoundException("User not found for identifier=\"" + key + "\"");
    }

    @Override
    public User create(boolean newIdentifier) {
        User user = getUP().get(newIdentifier);
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
    public Identifier getUserID(UserMultiKey userMultiKey, String idP) {
        Collection<User> users = get(userMultiKey, idP);
        if (users.size() == 1) return users.iterator().next().getIdentifier();
        throw new NotImplementedException("Error: getUserID for more than one found user id is nto yet ready.");
    }

    @Override
    public void update(User user, boolean noNewSerialID) {
        if (!noNewSerialID) {
            Identifier serialString = getUP().newIdentifier();
            user.setSerialIdentifier(serialString); // or subsequent calls have wrong serial string!
        }
        super.update(user);

    }

    @Override
    public void update(User t) {
        Identifier serialString = getUP().newIdentifier();
        t.setSerialIdentifier(serialString); // or subsequent calls have wrong serial string!
        super.update(t);
    }

    @Override
    public MapConverter getMapConverter() {
        return converter;
    }
}
