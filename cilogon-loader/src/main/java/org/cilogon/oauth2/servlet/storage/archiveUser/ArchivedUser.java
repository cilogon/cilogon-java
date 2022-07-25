package org.cilogon.oauth2.servlet.storage.archiveUser;

import edu.uiuc.ncsa.security.core.DateComparable;
import edu.uiuc.ncsa.security.core.Identifiable;
import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.util.IdentifiableImpl;
import org.cilogon.oauth2.servlet.storage.user.User;
import org.cilogon.oauth2.servlet.util.CILogonException;

import java.util.Date;

/**
 * Archived users are only comparable if they have the same userid. This lets us sort sets
 * of them (it doesn't really make sense to compare different versions of the same user's
 * data with another any way.)
 * <p>Created by Jeff Gaynor<br>
 * on Apr 9, 2010 at  11:24:18 PM
 */
public class ArchivedUser extends IdentifiableImpl implements Comparable, Identifiable, DateComparable {
    public ArchivedUser(Identifier identifier) {
        super(identifier);
    }


    public static final long serialVersionUID = 0xCafeD00dL;


    public Date archivedDate;

    public Identifier getArchivedID() {
        return getIdentifier();
    }

    public void setArchivedID(Identifier archivedID) {
        setIdentifier(archivedID);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User user;

    @Override
    public Date getCreationTS() {
        return getArchivedDate();
    }

    public Date getArchivedDate() {
        return archivedDate;
    }

    public void setArchivedDate(Date archivedDate) {
        this.archivedDate = archivedDate;
    }

    public int compareTo(Object o) {
        if (!(o instanceof ArchivedUser)) {
            throw new ClassCastException("Error: the given object is not of type ArchivedUser, but found\"" + o.getClass().getName() + "\"");
        }
        ArchivedUser x = (ArchivedUser) o;
        if (!x.getUser().getIdentifier().equals(getUser().getIdentifier())) {
            throw new CILogonException("Error: archived users are only comparable with those having the same ID");
        }

        return getArchivedDate().compareTo(x.getArchivedDate());
    }

    @Override
    public String toString() {
        return "ArchivedUser[userid=" + (getUser() == null ? "(null)" : getUser().getIdentifier()) + "]";
    }
}
