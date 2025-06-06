package org.cilogon.oauth2.servlet.storage.user;

import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.core.util.BeanUtils;
import edu.uiuc.ncsa.security.core.util.DateUtils;
import edu.uiuc.ncsa.security.core.util.StringUtils;
import edu.uiuc.ncsa.security.servlet.ServletDebugUtil;
import edu.uiuc.ncsa.security.storage.monitored.Monitored;
import net.sf.json.JSONObject;
import org.cilogon.oauth2.servlet.storage.sequence.SerialStrings;
import org.cilogon.oauth2.servlet.storage.transaction.AbstractCILServiceTransaction;
import org.cilogon.oauth2.servlet.util.DNUtil;

import static edu.uiuc.ncsa.security.core.util.BeanUtils.checkNoNulls;

/**
 * <p>Created by Jeff Gaynor<br>
 * on Mar 11, 2010 at  3:21:45 PM
 */
public class User extends Monitored {
    public static final long serialVersionUID = 0xCafeD00dL;

    public UserMultiID getUserMultiKey() {
        if (userMultiKey == null) {
            // Need an empty one. Easiest to construct one with a forced single null entry since the other are set null too.
            userMultiKey = new UserMultiID((EduPersonTargetedID) null);
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
        u2.setCreationTS(getCreationTS());
        u2.setLastModifiedTS(getLastModifiedTS());
        u2.setLastAccessed(getLastAccessed());
        u2.email = email;
        u2.idP = idP;
        u2.iDPName = iDPName;
        u2.lastName = lastName;
        UserMultiID userMultiKey2 = new UserMultiID(getRemoteUser(), getePPN(), getePTID(), getOpenID(), getOpenIDConnect(), getPairwiseID(), getSubjectID());
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

  /* @Override
    public Date getCreationTS() {
        return creationTS;
    }

    public void setCreationTS(Date creationTS) {
        this.creationTS = creationTS;
    }

    Date creationTS = new Date(); //default is now.*/


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
    String email="";
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

    String attr_json="";

    public boolean isUseUSinDN() {
        return useUSinDN;
    }

    public void setUseUSinDN(boolean useUSinDN) {
        this.useUSinDN = useUSinDN;
    }

    boolean useUSinDN = true;

    public String getSerialString() {
        if (serialString == null) {
            if (serialIdentifier != null) {
                serialString = serialStrings.toSerialString(serialIdentifier);
            }
        }
        return serialString;
    }

    public boolean canGetCert() {
        if (getDNState().canGetDN()) {
            return true; // already done
        }

        // problem is that updates may have made it possible to get a cert since the last time, so
        // *have* to check if the answer is no.
        getDNState().setFirstName(isNotTrivial(getFirstName()));
        getDNState().setLastName(isNotTrivial(getLastName()));
        getDNState().setDisplayName(isNotTrivial(getDisplayName()));
        getDNState().setIDPName(isNotTrivial(getIDPName()));
        getDNState().setEmail(isNotTrivial(getEmail()));
        // Or it won't be saved later.
        if (getState().isNullObject()) {
            // edge case
            JSONObject json = new JSONObject();
            json.put(DN_STATE, dnState.getStateValue());
            setState(json);
        } else {

            getState().put(DN_STATE, dnState.getStateValue());
        }
        return getDNState().canGetDN();
    }

    public boolean isNotTrivial(String x) {
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
    public String[] getCertName() {
        return getDNState().getDNNames(this);
    }

    public String getDN(AbstractCILServiceTransaction transaction, boolean returnEmail) {
        return DNUtil.getDN(this, transaction, returnEmail);
    }

    /**
     * A general JSON object for storing state about a user. Since we are getting more and more types of
     * users with often custom information it makes sense to have a <i>soft</i> structure to hold it (vs. changing the
     * user API generally for every special case).
     *
     * @return
     */
    public JSONObject getState() {
        if (state == null) {
            state = new JSONObject(); // darned irritating requirement from the JSON library or all puts throw an exception.
        }
        return state;
    }

    public void setState(JSONObject state) {
        this.state = state;
    }

    JSONObject state = new JSONObject();
    String DN_STATE = "dn_state";


    DNState dnState = null;

    public DNState getDNState() {
        if (dnState == null) {
            if (getState().containsKey(DN_STATE)) {
                dnState = new DNState(getState().getInt(DN_STATE));
            } else {
                // so this is completely new. Figure out if the user can get a cert here, before anything changes.
                dnState = new DNState();
                if (isNotTrivial(getEmail()) && isNotTrivial(getIDPName())) {
                    if (isNotTrivial(getFirstName()) && isNotTrivial(getLastName())) {
                        dnState.setStateValue(dnState.valid_flName);
                    }
                } else {
                    if (isNotTrivial(getDisplayName())) {
                        dnState.setStateValue(dnState.valid_dName);
                    }
                }
            }
        }
        return dnState;
    }

    public void setDNState(DNState dnState) {
        this.dnState = dnState;
        getState().put(DN_STATE, dnState.getStateValue());
    }

    public String toString() {
        return toString(0);
    }

    public String toString(int indent) {
        String pad = "";
        if (0 < indent) {
            pad = "\n" + StringUtils.getBlanks(indent);
        }
        String out = "User[";
        out = out + pad + "uid=\"" + getIdentifier() + "\",key=" + getUserMultiKey() + ",IdP=" + getIdP() + "\", ";
        out = out + pad + "serial string=\"" + getSerialString() + "\", ";
        out = out + pad + "first name=\"" + getFirstName() + "\", ";
        out = out + pad + "last name=\"" + getLastName() + "\", ";
        out = out + pad + "email=\"" + getEmail() + "\", ";
        out = out + pad + "idp display=\"" + getIDPName() + "\",";
        out = out + pad + "US IDP?=\"" + isUseUSinDN() + "\",";
        out = out + pad + "ou=" + getOrganizationalUnit() + ",affiliation=" + getAffiliation() + ",displayName=" + getDisplayName() + "\",";
        out = out + pad + "attr_json=" + getAttr_json();
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
        if (!DateUtils.compareDates(user.getCreationTS(), getCreationTS())) return false;
//        if (!user.getRemoteUser().equals(getRemoteUser())) return false;
        if (!checkEquals(user.getePPN(), getePPN())) return false;
        if (!checkEquals(user.getePTID(), getePTID())) return false;
        if (!checkEquals(user.getPairwiseID(), getPairwiseID())) return false;
        if (!checkEquals(user.getSubjectID(), getSubjectID())) return false;
        if (!checkEquals(user.getOpenID(), getOpenID())) return false;
        if (!checkEquals(user.getOpenIDConnect(), getOpenIDConnect())) return false;
        if (!checkEquals(user.getAffiliation(), getAffiliation())) return false;
        if (!checkEquals(user.getOrganizationalUnit(), getOrganizationalUnit())) return false;
        if (!checkEquals(user.getDisplayName(), getDisplayName())) return false;

        return true;

    }

    /**
     * Compare the IDP display name, first &amp; last names and email with this user. These are the items that
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

    public void setPairwiseId(PairwiseID pairwiseID) {
        userMultiKey.setPairwiseID(pairwiseID);
    }

    public void setSubjectId(SubjectID subjectID) {
        userMultiKey.setSubjectID(subjectID);
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

    public SubjectID getSubjectID() {
        if (hasSubjectID()) {
            return getUserMultiKey().getSubjectID();
        }
        return null;
    }

    public PairwiseID getPairwiseID() {
        if (hasPairwiseID()) {
            return getUserMultiKey().getPairwiseID();
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

    public boolean hasSubjectID() {
        if (userMultiKey == null) return false;
        return userMultiKey.hasSubjectID();
    }

    public boolean hasPairwiseID() {
        if (userMultiKey == null) return false;
        return userMultiKey.hasPairwiseID();
    }

    String affiliation = "";
    String organizationalUnit="";
    String displayName="";

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
