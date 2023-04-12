package org.cilogon.oauth2.servlet.storage.archiveUser;

import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.XMLConverter;
import edu.uiuc.ncsa.security.core.util.IdentifiableProviderImpl;
import edu.uiuc.ncsa.security.storage.GenericStoreUtils;
import edu.uiuc.ncsa.security.storage.MemoryStore;
import edu.uiuc.ncsa.security.storage.data.MapConverter;
import org.cilogon.oauth2.servlet.storage.user.User;
import org.cilogon.oauth2.servlet.storage.user.UserStore;
import org.cilogon.oauth2.servlet.util.ArchivedUserConverter;
import org.cilogon.oauth2.servlet.util.ArchivedUserStore;
import org.cilogon.oauth2.servlet.util.UserConverter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/13/12 at  2:37 PM
 */
public class MemoryArchivedUserStore extends MemoryStore<ArchivedUser> implements ArchivedUserStore {

    public MemoryArchivedUserStore(UserStore userStore, IdentifiableProviderImpl<ArchivedUser> aup) {
        super(aup);
        this.userStore = userStore;
    }

    UserStore userStore;
    HashMap<Identifier, CILFSArchivedUserStore.AUEntry> auEntries = new HashMap<Identifier, CILFSArchivedUserStore.AUEntry>();

    @Override
    public Identifier archiveUser(Identifier userId) {
        User u = userStore.get(userId);
        ArchivedUser au = create();
        au.setUser(u);
        CILFSArchivedUserStore.AUEntry auEntry = auEntries.get(userId);
        if (auEntry == null) {
            auEntry = new CILFSArchivedUserStore.AUEntry(userId);

        }
        auEntry.add(au);
        auEntries.put(userId, auEntry);
        save(au);
        return au.getArchivedID();
    }

    @Override
    public List<ArchivedUser> getAllByUserId(Identifier userId) {
        LinkedList<ArchivedUser> archivedUsers = new LinkedList<ArchivedUser>();
        CILFSArchivedUserStore.AUEntry aue = auEntries.get(userId);
        if (aue != null) {
            for (ArchivedUser x : aue) {
                archivedUsers.add(x);
            }
        }
        return archivedUsers;
    }

    @Override
    public ArchivedUser getLastArchivedUser(Identifier userid) {
        List<ArchivedUser> users = getAllByUserId(userid);
        if (users == null || users.isEmpty()) {
            return null;
        }
        return users.get(users.size() - 1);
    }

    public MapConverter getMapConverter() {
        ArchivedUserKeys keys = new ArchivedUserKeys();
        return new ArchivedUserConverter(keys, identifiableProvider, (UserConverter) userStore.getMapConverter());
    }

    @Override
    public XMLConverter<ArchivedUser> getXMLConverter() {
        return getMapConverter();
    }

    @Override
    public List<ArchivedUser> getMostRecent(int n, List<String> attributes) {
        return GenericStoreUtils.getMostRecent(this, n, attributes);
    }
}
