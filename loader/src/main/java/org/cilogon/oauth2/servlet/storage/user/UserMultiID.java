package org.cilogon.oauth2.servlet.storage.user;

import edu.uiuc.ncsa.security.core.exceptions.NotImplementedException;
import edu.uiuc.ncsa.security.core.util.BeanUtils;

import java.io.Serializable;
import java.util.Iterator;

/**
 * Once upon a time, the remote user and idp were sufficient to completely determine a user.
 * This key now takes the place of that at every turn. Any place that remote user would be used,
 * this class should be substituted.
 * <p>
 *     There is usually at most one of these for a user, so this is almost a facade for
 *     <ol>
 *         <li>{@link EduPersonPrincipleName}</li>
 *         <li>{@link EduPersonTargetedID}</li>
 *         <li>{@link OpenID}</li>
 *         <li>{@link OpenIDConnect}</li>
 *         <li>{@link PairwiseID}</li>
 *         <li>{@link SubjectID}</li>
 *         <li>{@link RemoteUserName}</li>
 *     </ol>
 * </p>
 * <p>Created by Jeff Gaynor<br>
 * on 5/6/14 at  9:32 AM
 */
public class UserMultiID implements Iterable<PersonName>, Serializable {
    EduPersonPrincipleName eppn;
    EduPersonTargetedID eptid;
    OpenID openID;
    RemoteUserName remoteUserName;
    SubjectID subjectID;

    public boolean hasSubjectID() {
        return hasPN(subjectID);
    }

    public boolean hasPairwiseID() {
        return hasPN(pairwiseID);

    }

    public SubjectID getSubjectID() {
        return subjectID;
    }

    public void setSubjectID(SubjectID subjectID) {
        this.subjectID = subjectID;
    }

    public PairwiseID getPairwiseID() {
        return pairwiseID;
    }

    public void setPairwiseID(PairwiseID persistentID) {
        this.pairwiseID = persistentID;
    }

    PairwiseID pairwiseID;

    public OpenIDConnect getOpenIDConnect() {
        return openIDConnect;
    }

    public void setOpenIDConnect(OpenIDConnect openIDConnect) {
        this.openIDConnect = openIDConnect;
    }

    OpenIDConnect openIDConnect;

    /**
     * Returns if this has only null or empty elements.
     *
     * @return
     */
    public boolean isTrivial() {
        return isTrivial(eppn) &&
                isTrivial(eptid) &&
                isTrivial(pairwiseID) &&
                isTrivial(subjectID) &&
                isTrivial(remoteUserName) &&
                isTrivial(openID) &&
                isTrivial(openIDConnect);
    }

    boolean isTrivial(PersonName personName) {
        //return personName == null || personName.isEmpty();
        return !hasPN(personName);
    }

    public UserMultiID(RemoteUserName remoteUserName) {
        this(remoteUserName, null, null, null, null, null, null);
    }

    public UserMultiID(EduPersonPrincipleName eppn) {
        this(null, eppn, null, null, null, null, null);
    }

    public UserMultiID(EduPersonTargetedID eptid) {
        this(null, null, eptid, null, null, null, null);
    }

    public UserMultiID(OpenID openID) {
        this(null, null, null, openID, null, null, null);
    }

    public UserMultiID(OpenID openID, OpenIDConnect openIDConnect) {
        this(null, null, null, openID, openIDConnect, null, null);
    }

    public UserMultiID(OpenIDConnect openIDConnect) {
        this(null, null, null, null, openIDConnect, null, null);
    }

    public UserMultiID(EduPersonPrincipleName eppn, EduPersonTargetedID eptid) {
        this(null, eppn, eptid, null, null, null, null);
    }

    public UserMultiID(EduPersonPrincipleName eppn,
                       EduPersonTargetedID eptid,
                       PairwiseID pairwiseID,
                       SubjectID subjectID) {
        this(null, eppn, eptid, null, null, pairwiseID, subjectID);
    }

    public UserMultiID(RemoteUserName remoteUserName,
                       EduPersonPrincipleName eppn,
                       EduPersonTargetedID eptid,
                       OpenID openID,
                       OpenIDConnect openIDConnect,
                       PairwiseID pairwiseID,
                       SubjectID subjectID) {
        this.eppn = eppn;
        this.eptid = eptid;
        this.openID = openID;
        this.remoteUserName = remoteUserName;
        this.openIDConnect = openIDConnect;
        this.pairwiseID = pairwiseID;
        this.subjectID = subjectID;
    }

    public UserMultiID(RemoteUserName remoteUserName,
                       EduPersonPrincipleName eppn,
                       EduPersonTargetedID eptid,
                       OpenID openID,
                       OpenIDConnect openIDConnect) {
        this.eppn = eppn;
        this.eptid = eptid;
        this.openID = openID;
        this.remoteUserName = remoteUserName;
        this.openIDConnect = openIDConnect;
    }

    public void setEppn(EduPersonPrincipleName eppn) {
        this.eppn = eppn;
    }

    public void setEptid(EduPersonTargetedID eptid) {
        this.eptid = eptid;
    }

    public void setOpenID(OpenID openID) {
        this.openID = openID;
    }

    public void setRemoteUserName(RemoteUserName remoteUserName) {
        this.remoteUserName = remoteUserName;
    }

    public EduPersonPrincipleName getEppn() {
        return eppn;
    }

    public EduPersonTargetedID getEptid() {
        return eptid;
    }

    public OpenID getOpenID() {
        return openID;
    }

    public RemoteUserName getRemoteUserName() {
        return remoteUserName;
    }

    @Override
    public Iterator<PersonName> iterator() {
        Iterator<PersonName> iterator = new Iterator<PersonName>() {
            PersonName[] list = new PersonName[]{eppn, eptid, remoteUserName, openID, openIDConnect, pairwiseID, subjectID};
            int counter = 0; // overload value to show no iteration yet

            @Override
            public boolean hasNext() {
                if (counter == list.length - 1) return false;
                return true;
            }

            @Override
            public PersonName next() {
                return list[++counter];
            }

            @Override
            public void remove() {
                throw new NotImplementedException("Error: This is not implemented");
            }
        };
        return iterator;
    }

    protected boolean checkEquals(PersonName p1, PersonName p2) {
        if (p1 == null) {
            if (p2 != null && (p2.getName() == null || p2.getName().equals(""))) {
                return true;
            }
            return false;
        }
        if (p2 == null) {
            if (p1 != null && (p1.getName() == null || p1.getName().equals(""))) {
                return true;
            }
            return false;
        }

        // checkBasic assumes that nullity conditions have been met already.
        if (!BeanUtils.checkBasic(p1, p2)) return false;
        return p1.getName().equals(p2.getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof UserMultiID)) return false;
        UserMultiID umk = (UserMultiID) obj;
        if (!checkEquals(getRemoteUserName(), umk.getRemoteUserName())) return false;
        if (!checkEquals(getEppn(), umk.getEppn())) return false;
        if (!checkEquals(getEptid(), umk.getEptid())) return false;
        if (!checkEquals(getOpenID(), umk.getOpenID())) return false;
        if (!checkEquals(getOpenIDConnect(), umk.getOpenIDConnect())) return false;
        if (!checkEquals(getPairwiseID(), umk.getPairwiseID())) return false;
        if (!checkEquals(getSubjectID(), umk.getSubjectID())) return false;
        return true;
    }

    private UserMultiID() {
    }

    String which(PersonName oldName, PersonName newName) {
        String x = null;
        if (oldName != null) {
            x = oldName.getName();
        }
        String newValue = null;
        if (newName != null) {
            newValue = newName.getName();
        }
        if (x == null) {
            if (newValue == null) {
                return null;
            } else {
                return newValue;
            }
        } else {
            if (newValue == null) {
                return x;
            } else {
                // What this means is that a new value can replace an old one as an update.
                return newValue;
            }
        }
    }

    public boolean keepSerialString(UserMultiID newID) {
        // these are done in order because there e.g. eppn is immutable, but eptid is not.
        // ALSO in the DBService there is a userLogic call that is invoke for the findUser function
        // that ferrets out edge cases and throws exceptions, so by assumption, this has been checked and is
        // sane at this point and we only have to determine if something changed. 
        if (hasEPPN() && !getEppn().equals(newID.getEppn())) return false;
        if (!hasEPPN() && hasEPTID() && !getEptid().equals(newID.getEptid()))
            return false; // eppn unchanged, eptid can change.
//        if (hasOpenID() && !getOpenID().equals(newID.getOpenID())) return false;
        //      if (hasRemoteUser() && !getRemoteUserName().equals(newID.getRemoteUserName())) return false;

        return true;
    }

    /**
     * Centralizing checking if a newid should update this. This can be a rat's nest since we may effectively
     * get multiple user names with different calls.
     *
     * @param newid
     * @return
     */
    public UserMultiID union(UserMultiID newid) {

        UserMultiID out = new UserMultiID();
        String v = which(getRemoteUserName(), newid.getRemoteUserName());
        if (v != null) {
            RemoteUserName r = new RemoteUserName(v);
            out.setRemoteUserName(r);
        }
        v = which(getEppn(), newid.getEppn());
        if (v != null) {
            EduPersonPrincipleName eppn = new EduPersonPrincipleName(v);
            out.setEppn(eppn);
        }
        v = which(getEptid(), newid.getEptid());
        if (v != null) {
            EduPersonTargetedID eptid = new EduPersonTargetedID(v);
            out.setEptid(eptid);
        }
        v = which(getPairwiseID(), newid.getPairwiseID());
        if (v != null) {
            PairwiseID pairwiseID = new PairwiseID(v);
            out.setPairwiseID(pairwiseID);
        }
        v = which(getSubjectID(), newid.getSubjectID());
        if (v != null) {
            SubjectID subjectID = new SubjectID(v);
            out.setSubjectID(subjectID);
        }
        v = which(getOpenID(), newid.getOpenID());
        if (v != null) {
            OpenID openID = new OpenID(v);
            out.setOpenID(openID);
        }
        v = which(getOpenIDConnect(), newid.getOpenIDConnect());
        if (v != null) {
            OpenIDConnect openIDConnect = new OpenIDConnect(v);
            out.setOpenIDConnect(openIDConnect);
        }

        return out;
    }

    public boolean hasEPPN() {
        return hasPN(eppn);
    }

    public boolean hasEPTID() {
        return hasPN(eptid);
    }

    public boolean hasOpenID() {
        return hasPN(openID);
    }

    public boolean hasRemoteUser() {
        return hasPN(remoteUserName);
    }

    public boolean hasOpenIDConnect() {
        return hasPN(openIDConnect);
    }

    protected boolean hasPN(PersonName pn) {
        if (pn == null) return false;
        if (pn.getName() == null || 0 == pn.getName().length()) return false;
        return true;
    }

    @Override
    public String toString() {
        String x = getClass().getSimpleName() + "[";
        boolean gotOne = false;
        if (hasRemoteUser()) {
            x = x + "remoteUser=" + getRemoteUserName().getName();
            gotOne = true;
        }
        if (hasEPTID()) {
            x = x + (gotOne ? "," : "") + "eptid=" + getEptid().getName();
            gotOne = true;
        }
        if (hasPairwiseID()) {
            x = x + (gotOne ? "," : "") + "pairwise_id=" + getPairwiseID().getName();
            gotOne = true;
        }
        if (hasSubjectID()) {
            x = x + (gotOne ? "," : "") + "subject_id=" + getSubjectID().getName();
            gotOne = true;
        }
        if (hasEPPN()) {
            x = x + (gotOne ? "," : "") + "eppn=" + getEppn().getName();
            gotOne = true;
        }
        if (hasOpenID()) {
            x = x + (gotOne ? "," : "") + "openid=" + getOpenID().getName();
            gotOne = true;
        }
        if (hasOpenIDConnect()) {
            x = x + (gotOne ? "," : "") + "oidc=" + getOpenIDConnect().getName();
        }

        x = x + "]";
        return x;
    }
}
