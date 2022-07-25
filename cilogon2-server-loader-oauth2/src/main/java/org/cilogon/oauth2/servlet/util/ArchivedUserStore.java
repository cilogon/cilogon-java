package org.cilogon.oauth2.servlet.util;

import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.Store;
import org.cilogon.oauth2.servlet.storage.archiveUser.ArchivedUser;

import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on Jun 22, 2010 at  8:23:03 PM
 */
public interface ArchivedUserStore extends Store<ArchivedUser> {
    /**
     * Get the list of all archived users for a given user (as opposed to archive) id.
     * This always returns a list sorted by archive date.
     *
     * @param userId
     * @return
     */
    public List<ArchivedUser> getAllByUserId(Identifier userId);

    /**
     * Add a User to the archive. This takes the current user as stored (so save any updates before invoking).
     * Returns the id of the archived user entry.
     *
     * @param userId
     */
    public Identifier archiveUser(Identifier userId);


    /**
     * Convenience method. Return the most recently archived user for this id or a null if there is no
     * such user.
     *
     * @param userid
     * @return
     */
    public ArchivedUser getLastArchivedUser(Identifier userid);
}
