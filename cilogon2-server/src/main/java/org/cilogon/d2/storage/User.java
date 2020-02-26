package org.cilogon.d2.storage;

import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.core.util.BeanUtils;
import edu.uiuc.ncsa.security.core.util.DateUtils;
import edu.uiuc.ncsa.security.core.util.IdentifiableImpl;
import edu.uiuc.ncsa.security.servlet.ServletDebugUtil;
import org.cilogon.d2.util.AbstractCILServiceTransaction;
import org.cilogon.d2.util.DNUtil;
import org.cilogon.d2.util.SerialStrings;

import java.util.Date;

import static edu.uiuc.ncsa.security.core.util.BeanUtils.checkNoNulls;

/**
 * <p>Created by Jeff Gaynor<br>
 * on Mar 11, 2010 at  3:21:45 PM
 */
public class User extends IdentifiableImpl {
    public static final long serialVersionUID = 0xCafeD00dL;

    public UserMultiID getUserMultiKey() {
        if (userMultiKey == null) {
            userMultiKey = new UserMultiID(getRemoteUser(), getePPN(), getePTID(), getOpenID(), getOpenIDConnect());
        }
        return userMultiKey;
    }

    public void setUserMultiKey(UserMultiID userMultiKey) {
        this.userMultiKey = userMultiKey;
    }

    UserMultiID userMultiKey;

    /**
     * Returns a completely new user whose information is identical to this user.
     *
     * @return
     */

    @Override
    public User clone() {
        User u2 = new User(getIdentifier(), serialStrings);
        copyTo(u2, false);
        return u2;
    }

    /**
     * Copy this user into the given user. This replaces the current user's values with the arguments. Mostly this
     * is of use in certain recovery utilities and it is logical that the user class manage this. Optionally,
     * reset the target's identifier to this on
     *
     * @param u2
     * @param copyID whether or not to copy this user's id to the target.
     */
    public void copyTo(User u2, boolean copyID) {
        if (copyID) {
            u2.setIdentifier(getIdentifier());
        }
        u2.firstName = firstName;
        u2.lastName = lastName;
        u2.creationTime = creationTime;
        u2.email = email;
        u2.idP = idP;
        u2.iDPName = iDPName;
        u2.lastName = lastName;
        UserMultiID userMultiKey2 = new UserMultiID(getRemoteUser(), getePPN(), getePTID(), getOpenID(), getOpenIDConnect());
        u2.setUserMultiKey(userMultiKey2);
        u2.serialIdentifier = serialIdentifier;
        u2.affiliation = affiliation;
        u2.organizationalUnit = organizationalUnit;
        u2.displayName = displayName;
        u2.useUSinDN = useUSinDN;
    }

    public User(Identifier uid,
                String firstName,
                String lastName,
                String idP,
                String iDPName,
                String email,
                String serialString) {
        super(uid);
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.iDPName = iDPName;
        this.idP = idP;
        this.serialString = serialString;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    Date creationTime = new Date(); //default is now.


    public User(Identifier id, SerialStrings ss) {
        super(id);
        serialStrings = ss;
        serialIdentifier = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIDPName() {
        return iDPName;
    }

    public void setIDPName(String iDPName) {
        this.iDPName = iDPName;
    }

    public String getIdP() {
        return idP;
    }

    public void setIdP(String idP) {
        this.idP = idP;
    }

    String firstName;
    String lastName;
    String email;
    String iDPName; //remote_user
    String idP;

    /**
     * This is used in cases where the member of information is sent via a call to the getUser DB endpoint.
     *
     * @return
     */
    public String getAttr_json() {
        return attr_json;
    }

    public void setAttr_json(String attr_json) {
        this.attr_json = attr_json;
    }

    String attr_json;

    public boolean isUseUSinDN() {
        return useUSinDN;
    }

    public void setUseUSinDN(boolean useUSinDN) {
        this.useUSinDN = useUSinDN;
    }

    boolean useUSinDN = false;

    public String getSerialString() {
        if (serialString == null) {
            if (serialIdentifier != null) {
                serialString = serialStrings.toSerialString(serialIdentifier);
            }
        }
        return serialString;
    }

    public boolean canGetCert() {
        boolean canGetCert = isNotTrivial(getFirstName()) && isNotTrivial(getLastName());
        canGetCert = canGetCert || isNotTrivial(getDisplayName());
        canGetCert = canGetCert && isNotTrivial(getEmail());

        return canGetCert;
    }

    boolean isNotTrivial(String x) {
        return (x != null && !x.isEmpty());
    }

    public void setSerialString(String serialString) {
        ServletDebugUtil.trace(this, "Setting user serial string to " + serialString, new GeneralException());
        if (this.serialString == null) {
            // syntax check. Simple but catches a lot of errors.
            if (0 != serialString.split("[A-Za-z]+[0-9]+").length) {
                // then this is not of the correct form.
                throw new IllegalArgumentException("Error: the given serial string \"" + serialString + "\" is not of the correct form " +
                        "for user \"" + getIdentifier() + "\"");
            }
            this.serialString = serialString;
        }
        serialIdentifier = null; //reset it.
    }

    public RemoteUserName getRemoteUser() {
        if (hasRemoteUser()) {
            return getUserMultiKey().getRemoteUserName();
        }
        return null;
    }

    public void setRemoteUser(RemoteUserName remoteUser) {
        getUserMultiKey().setRemoteUserName(remoteUser);
    }

    String serialString;


    public Identifier getSerialIdentifier() {
        if (serialIdentifier == null) {
            if (serialString != null) {
                serialIdentifier = BasicIdentifier.newID(serialStrings.fromSerialString(serialString));
            }
        }
        return serialIdentifier;
    }

    transient SerialStrings serialStrings;


    public void setSerialIdentifier(Identifier serialIdentifier) {
        // Fix for CIL-68. Allow resets
        serialString = null;
        this.serialIdentifier = serialIdentifier;
    }


    Identifier serialIdentifier;

    /**
     * Only call this if {@link #canGetCert()} is true.
     *
     * @return
     */
    public String getCertName() {
        if (isNotTrivial(getFirstName()) && isNotTrivial(getLastName())) {
            return getFirstName() + " " + getLastName();
        }
        if (isNotTrivial(getDisplayName())) {
            return getDisplayName();
        }
        return null;
    }

    public String getDN(AbstractCILServiceTransaction transaction, boolean returnEmail) {
        return DNUtil.getDN(this, transaction, returnEmail);
    }

    public String toString() {
        String out = "User[uid=\"" + getIdentifier() + "\",key=" + getUserMultiKey() + ",IdP=" + getIdP() + "\", ";
        out = out + "serial string=\"" + getSerialString() + "\", ";
        out = out + "first name=\"" + getFirstName() + "\", ";
        out = out + "last name=\"" + getLastName() + "\", ";
        out = out + "email=\"" + getEmail() + "\", ";
        out = out + "idp display=\"" + getIDPName() + "\",";
        out = out + "US IDP?=\"" + isUseUSinDN() + "\",";
        out = out + "ou=" + getOrganizationalUnit() + ",affiliation=" + getAffiliation() + ",displayName=" + getDisplayName() + "\",";
        out = out + "attr_json=" + getAttr_json();
        out = out + "]";
        return out;
    }

    public boolean checkEquals(PersonName x, PersonName y) {
        if (x == y) return true;
        if (!BeanUtils.checkNoNulls(x, y)) return false;
        return checkEquals(x.getName(), y.getName());

    }

    public boolean checkEquals(String x, String y) {
        if (x == y) return true;
        if (x == null && y != null) {
            // We agree that empty and null strings are the same.
            if (y.length() == 0) return true;
            return false;
        }
        if (x != null && y == null) {
            if (x.length() == 0) return true;
            return false;
        }
        return x.equals(y);
    }

    /**
     * Note especially that this compares users as <b>java objects</b>, not a logical users.
     * The method {@link #compare(User)} should be user for comparing users. See the
     * note there.
     *
     * @param obj
     * @return
     */
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof User)) return false;
        if (obj == this) return true; // same instance
        User user = (User) obj;
        if (!compare(user)) return false;
        if (!checkNoNulls(user.getSerialIdentifier(), getSerialIdentifier())) return false;
        if (!checkNoNulls(user.getIdentifier(), getIdentifier())) ;
        if (!DateUtils.compareDates(user.getCreationTime(), getCreationTime())) return false;
//        if (!user.getRemoteUser().equals(getRemoteUser())) return false;
        if (!checkEquals(user.getePPN(), getePPN())) return false;
        if (!checkEquals(user.getePTID(), getePTID())) return false;
        if (!checkEquals(user.getOpenID(), getOpenID())) return false;
        if (!checkEquals(user.getOpenIDConnect(), getOpenIDConnect())) return false;
        if (!checkEquals(user.getAffiliation(), getAffiliation())) return false;
        if (!checkEquals(user.getOrganizationalUnit(), getOrganizationalUnit())) return false;
        if (!checkEquals(user.getDisplayName(), getDisplayName())) return false;

        return true;

    }

    /**
     * Compare the IDP display name, first & last names and email with this user. These are the items that
     * go into the certificate so if there is a change in these, this method will return true.
     *
     * @param idpDisplayName
     * @param firstName
     * @param lastName
     * @param email
     * @return
     */

    public boolean compare(String idpDisplayName,
                           String firstName,
                           String lastName,
                           String email
    ) {
        if (!checkEquals(getEmail(), email)) return false;
        if (!checkEquals(getFirstName(), firstName)) return false;
        if (!checkEquals(getIDPName(), idpDisplayName)) return false;
        if (!checkEquals(getLastName(), lastName)) return false;

        return true;
    }

    /**
     * This compares the 6 major keys of the given user to this. This is important, since
     * equals compares Java objects. We have to compare the actual 6 parameters to see if,
     * for instance, a user should be archived and issued a new serial string.
     *
     * @param user
     * @return
     */
    public boolean compare(User user) {
        if (!checkEquals(user.getRemoteUser(), getRemoteUser())) return false;
        if (!checkEquals(user.getIdP(), getIdP())) return false;
        if (!compare(user.getIDPName(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail())) return false;
        return true;
    }

    /*
     * Following block resolves OAUTH-108.
     */

    public EduPersonPrincipleName getePPN() {
        if (hasEPPN()) {
            return getUserMultiKey().getEppn();
        }
        return null;
    }

    public void setePPN(EduPersonPrincipleName ePPN) {
        userMultiKey.setEppn(ePPN);
    }

    public EduPersonTargetedID getePTID() {
        if (hasEPTID()) {
            return getUserMultiKey().getEptid();
        }
        return null;
    }

    public void setePTID(EduPersonTargetedID ePTID) {
        userMultiKey.setEptid(ePTID);
    }

    public OpenID getOpenID() {
        if (hasOpenID()) {
            return getUserMultiKey().getOpenID();
        }
        return null;
    }

    public void setOpenID(OpenID openID) {
        getUserMultiKey().setOpenID(openID);
    }


    public OpenIDConnect getOpenIDConnect() {
        if (hasOpenIDConnect()) {
            return getUserMultiKey().getOpenIDConnect();
        }
        return null;
    }

    public void setOpenIDConnect(OpenIDConnect openIDConnect) {
        userMultiKey.setOpenIDConnect(openIDConnect);
    }

    public boolean hasOpenIDConnect() {
        if (userMultiKey == null) return false;
        return userMultiKey.hasOpenIDConnect();
    }

    public boolean hasEPPN() {
        if (userMultiKey == null) return false;
        return userMultiKey.hasEPPN();
    }

    public boolean hasEPTID() {
        if (userMultiKey == null) return false;
        return userMultiKey.hasEPTID();
    }

    public boolean hasRemoteUser() {
        if (userMultiKey == null) return false;
        return userMultiKey.hasRemoteUser();
    }

    public boolean hasOpenID() {
        if (userMultiKey == null) return false;
        return userMultiKey.hasOpenID();
    }

    String affiliation;
    String organizationalUnit;
    String displayName;

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getOrganizationalUnit() {
        return organizationalUnit;
    }

    public void setOrganizationalUnit(String organizationalUnit) {
        this.organizationalUnit = organizationalUnit;
    }

}
