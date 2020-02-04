package org.cilogon.d2.storage;

import edu.uiuc.ncsa.security.core.Identifiable;
import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.Store;
import edu.uiuc.ncsa.security.storage.data.MapConverter;

import java.util.Collection;

/**
 * One note is about saving users: Invoking {@link #save(edu.uiuc.ncsa.security.core.Identifiable)} on a user
 * should either register the user (if not in the store) or update the user. Updating a user means
 * changing the serial string of the user. Do not save a user unless there is a bona fide change to their
 * information on this account!<br><br>
 * What's more, users in a store are independent of any instances returned by the store (so some cloning on
 * saves and gets might be required). This is because of archiving semantics: The sequence is determine if the
 * current instance of a user has changed, invoke ArchivedUserStore.archiveUser, which gets the old stored
 * in this store, and archives it. Then you may save the user instance.
 * <p>Created by Jeff Gaynor<br>
 * on Mar 12, 2010 at  12:55:02 PM
 */
public interface UserStore extends Store<User> {

    /**
     * Save the current user but do <b>not</b> change the serial identifier. Note that this circumvents
     * the contract for {@link Store#update(edu.uiuc.ncsa.security.core.Identifiable)} for this interface,
     * which, because of issue CIL-69 requires it in most cases. This is in effect an internal call that
     * allows for administrative updates. If <code>noNewSerialID = false</code>, then the effect is the same
     * as calling {@link #update(Identifiable)} and a new serial id is created.
     *
     * @param noNewSerialID
     */
    public void update(User user, boolean noNewSerialID);

    /**
     * The remote user and idp together form a composite key for access. (Our URIs are internally generated unique identifiers
     * and are not used outside of the store).
     *
     * @param idP
     * @return
     */
    //  public User get(String remoteUser, String idP);
    public Collection<User> get(UserMultiKey userMultiKey, String idP);

    /**
     * Create a user from all the given possible information. Some of this might
     * be missing, but the minimal set is the remoteUser and idp. Note that this
     * creates the user uid too AND it registers the user.
     *
     * @param userMultiKey
     * @param idP
     * @param idPDisplayName
     * @param firstName
     * @param lastName
     * @param email
     * @return
     */
    public User createAndRegisterUser(UserMultiKey userMultiKey,
                                      String idP,
                                      String idPDisplayName,
                                      String firstName,
                                      String lastName,
                                      String email,
                                      String affiliation,
                                      String displayName,
                                      String organizationalUnit);


    /**
     * Resolves the user id given the remoteUser and IdP.
     *
     * @param userMultiKey
     * @param idP
     * @return
     */
    public Identifier getUserID(UserMultiKey userMultiKey, String idP);

    /**
     * Create a user, optionally with a new serial string. The default behavior for this store
     * is that new users are not created with a new serial identifier (or various operations
     * such as listing users can run through a great many unused identifiers and if they
     * are expensive to create, such as from an SQL database, this might slow down the application
     * noticeably.)
     *
     * @param newSerialString
     * @return
     */
    public User create(boolean newSerialString);

    public MapConverter getMapConverter();
}
