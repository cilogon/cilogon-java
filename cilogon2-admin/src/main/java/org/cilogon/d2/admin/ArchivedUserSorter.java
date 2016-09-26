package org.cilogon.d2.admin;

import edu.uiuc.ncsa.myproxy.oa4mp.server.ClientSorter;
import edu.uiuc.ncsa.security.core.Identifiable;
import edu.uiuc.ncsa.security.core.util.Iso8601;
import edu.uiuc.ncsa.security.delegation.storage.Client;
import org.cilogon.d2.storage.ArchivedUser;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 5/22/14 at  5:02 PM
 */
public class ArchivedUserSorter extends ClientSorter {
    @Override
    public ArrayList<Identifiable> sort(List<Identifiable> arg) {
        TreeMap<String, Identifiable> tm = new TreeMap<>();

        for (int i = 0; i < arg.size(); i++) {
            ArchivedUser archivedUser = (ArchivedUser) arg.get(i);
            if (sortOnDates) {
                tm.put(Iso8601.date2String(archivedUser.getArchivedDate()), archivedUser);
            } else if (sortOnIds) {
                tm.put(archivedUser.getIdentifierString(), archivedUser);
            }
        }
        return new ArrayList(tm.values());
    }
}
