package org.cilogon.oauth2.servlet.storage.archiveUser;

import edu.uiuc.ncsa.myproxy.oa4mp.server.OA4MPConfigTags;
import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.util.IdentifiableImpl;
import edu.uiuc.ncsa.security.core.util.IdentifiableProviderImpl;
import edu.uiuc.ncsa.security.storage.FileStore;
import edu.uiuc.ncsa.security.storage.GenericStoreUtils;
import edu.uiuc.ncsa.security.storage.data.MapConverter;
import org.cilogon.oauth2.servlet.storage.user.User;
import org.cilogon.oauth2.servlet.storage.user.UserStore;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

       /*
        NOTE the archived user file store is not implemented (see note below) and all the tests for it here
        will fail.
        The reason is that databases kee pa separate table for users but the file store has to
        manage a thing called an AUEntry (ArchivedUserEntry) and there is no converter for that
        to serialize it. At this poiint (7/19/2018) it is still doubtful anyone wants to put
        CILogon on a file system, but enough of the code exists that it should be kept for now.
         */

/**
 * This actually creates and manages a store of old users that are recorded in a specific data structure.
 * The reason is that we must be able to look up old users by their ID, of which there may be many.
 * So we get lists of archived user entries which are are only distinguishable by their archive date.
 * <p>Created by Jeff Gaynor<br>
 * on 3/9/12 at  11:18 AM
 */
public class CILFSArchivedUserStore extends FileStore<ArchivedUser> implements ArchivedUserStore {

    public CILFSArchivedUserStore(File dataPath,
                                  File indexPath,
                                  UserStore userStore,
                                  IdentifiableProviderImpl<ArchivedUser> aup,
                                  MapConverter archivedUserMapConverter,
                                  boolean removeEmptyFiles,
                                  boolean removeFailedFiles) {
        super(dataPath, indexPath, aup, archivedUserMapConverter, removeEmptyFiles, removeFailedFiles);
        this.userStore = userStore;
        File f = new File(dataPath.getParent(), OA4MPConfigTags.ARCHIVED_USERS);
        f.mkdirs(); // just in case we need to create this on the fly.
        auEntryFileStore = new AUEntryFileStore(f);
    }

    protected static class AUEntryFileStore extends FileStore<AUEntry> {
        // Caveat: This sets the converter to null, so that plain old java serialization is used.
        // Since this shouldn't really ever change, that should be ok.
        public AUEntryFileStore(File file) {
            super(file,  null,null, true, true);
        }

        public AUEntryFileStore(File storeDirectory, File indexDirectory) {
            super(storeDirectory, indexDirectory, null, null, true, true);
        }

        @Override
        public List<AUEntry> getMostRecent(int n, List<String> attributes) {
            throw new UnsupportedOperationException();
        }

        @Override
        public AUEntry create() {
            return new AUEntry(null);
        }
    }

    public static class AUEntry extends IdentifiableImpl implements Iterable<ArchivedUser> {
        public AUEntry(Identifier identifier) {
            super(identifier);
        }

        public static final long serialVersionUID = 0xCafeD00dL;
        LinkedList<ArchivedUser> auList = new LinkedList<ArchivedUser>();

        public void add(ArchivedUser archivedUser) {
            if (auList.isEmpty()) {
                setIdentifier(archivedUser.getUser().getIdentifier());
            } else {
                if (!archivedUser.getUser().getIdentifier().equals(getIdentifier())) {
                    throw new IllegalArgumentException("Error: Incompatible user added. Was \"" + archivedUser.getUser().getIdentifier() + "\", need \"" + getIdentifier() + "\".");
                }
            }
            auList.add(archivedUser);
        }

        @Override
        public Iterator<ArchivedUser> iterator() {
            return auList.iterator();
        }

    }

    UserStore userStore;

    AUEntryFileStore auEntryFileStore;


    @Override
    public Identifier archiveUser(Identifier userId) {
        User u = userStore.get(userId);
        AUEntry auEntry = auEntryFileStore.get(userId);
        if (auEntry == null) {
            auEntry = auEntryFileStore.create();
        }
        // no user means a UserNotFoundException has been thrown by this point.
        ArchivedUser au = create();
        au.setUser(u);
        save(au);
        auEntry.add(au);
        auEntryFileStore.save(auEntry);
        return au.getArchivedID();
    }

    @Override
    public List<ArchivedUser> getAllByUserId(Identifier userId) {
        AUEntry auEntry = auEntryFileStore.get(userId);
        // create and return a list of these so that the requester can e.g. iterate over it without
        // getting errors.
        LinkedList<ArchivedUser> aus = new LinkedList<ArchivedUser>();
        if (auEntry == null) {
            return aus;
        }
        for (ArchivedUser x : auEntry) {
            aus.add(x);
        }
        return aus;
    }

    @Override
    public ArchivedUser getLastArchivedUser(Identifier userid) {
        List<ArchivedUser> users = getAllByUserId(userid);
        if (users.isEmpty()) {
            return null;
        }
        return users.get(users.size() - 1);
    }

    @Override
    public List<ArchivedUser> getMostRecent(int n, List<String> attributes) {
        return GenericStoreUtils.getMostRecent(this, n, attributes);
    }
}
