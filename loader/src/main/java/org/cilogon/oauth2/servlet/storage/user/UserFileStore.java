package org.cilogon.oauth2.servlet.storage.user;

import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.core.exceptions.NFWException;
import edu.uiuc.ncsa.security.core.exceptions.NotImplementedException;
import edu.uiuc.ncsa.security.core.util.IdentifiableProviderImpl;
import edu.uiuc.ncsa.security.storage.GenericStoreUtils;
import edu.uiuc.ncsa.security.storage.data.MapConverter;
import edu.uiuc.ncsa.security.storage.monitored.MonitoredFileStore;
import org.cilogon.oauth2.servlet.util.Incrementable;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/2/12 at  12:52 PM
 */
public class UserFileStore extends MonitoredFileStore<User> implements UserStore {
    @Override
    public Incrementable getIncrementable() {
        return incrementable;
    }

    Incrementable incrementable;

    public UserFileStore(File file, UserProvider up, MapConverter converter,
                         boolean removeEmptyFiles, boolean removeFailedFiles) {
        super(file, up, converter, removeEmptyFiles, removeFailedFiles);
    }

    protected UserProvider getUP() {
        return (UserProvider) identifiableProvider;
    }

    static final String INFIX_STRING = "&";

    public UserFileStore(File storeDirectory,
                         File indexDirectory,
                         IdentifiableProviderImpl<User> up,
                         MapConverter converter,
                         boolean removeEmptyFiles,
                         Incrementable incrementable,
                         boolean removeFailedFiles) {
        super(storeDirectory, indexDirectory, up, converter, removeEmptyFiles, removeFailedFiles);
        this.incrementable = incrementable;
    }

    @Override
    public void save(User t) {
        t.setLastModifiedTS(new Date());
        super.save(t);
    }

    @Override
    public void realSave(boolean checkExists, User t) {
        t.setLastModifiedTS(new Date());
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
    public User createAndRegisterUser(UserMultiID userMultiKey,
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
        if (remoteUser instanceof SubjectID) return toIdentifier((SubjectID) remoteUser, idp);
        if (remoteUser instanceof PairwiseID) return toIdentifier((PairwiseID) remoteUser, idp);
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

    protected String toIdentifier(SubjectID subjectID, String idp) {
        String x = uk().subjectId() + ":" + subjectID.getName() + INFIX_STRING + idp;
        return x;
    }

    protected String toIdentifier(PairwiseID pairwiseID, String idp) {
        String x = uk().pairwiseId() + ":" + pairwiseID.getName() + INFIX_STRING + idp;
        return x;
    }

    protected String toIdentifier(String remoteUser, String idp) {
        String x = remoteUser + INFIX_STRING + idp;
        return x;
    }

    @Override
    public Collection<User> get(UserMultiID userMultiKey, String idP) {
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
        } catch (Throwable t) {
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
    public Identifier getUserID(UserMultiID userMultiKey, String idP) {
        Collection<User> users = get(userMultiKey, idP);
        if (users.size() == 1) return users.iterator().next().getIdentifier();
        throw new NotImplementedException("Error: getUserID for more than one found user id is nto yet ready.");
    }

    @Override
    public void updateCheckSerialString(User user, boolean noNewSerialID) {
        if (!noNewSerialID) {
            Identifier serialString = getUP().newIdentifier();
            user.setSerialIdentifier(serialString); // or subsequent calls have wrong serial string!
        }
        user.setLastModifiedTS(new Date());

        super.update(user);

    }

/*
    @Override
    public void update(User user, boolean noNewSerialID) {
        if (!noNewSerialID) {
            Identifier serialString = getUP().newIdentifier();
            user.setSerialIdentifier(serialString); // or subsequent calls have wrong serial string!
        }
        user.setLastModifiedTS(new Date());

        super.update(user);

    }
*/

    @Override
    public void update(User t) {
        Identifier serialString = getUP().newIdentifier();
        t.setSerialIdentifier(serialString); // or subsequent calls have wrong serial string!
        t.setLastModifiedTS(new Date());
        super.update(t);
    }

    @Override
    public MapConverter getMapConverter() {
        return converter;
    }

    @Override
    public List<User> getMostRecent(int n, List<String> attributes) {
        return GenericStoreUtils.getMostRecent(this, n, attributes);
    }
}
