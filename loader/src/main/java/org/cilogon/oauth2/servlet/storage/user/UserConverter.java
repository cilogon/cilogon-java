package org.cilogon.oauth2.servlet.storage.user;

import edu.uiuc.ncsa.security.core.IdentifiableProvider;
import edu.uiuc.ncsa.security.storage.data.*;
import edu.uiuc.ncsa.security.storage.monitored.*;
import net.sf.json.JSONObject;

import static edu.uiuc.ncsa.security.core.util.BasicIdentifier.newID;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/16/12 at  5:34 PM
 */
public class UserConverter<T extends User> extends MonitoredConverter<T> {
    public UserConverter(MonitoredKeys keys, IdentifiableProvider<T> vProvider) {
        super(keys, vProvider);
    }

    public UserConverter(IdentifiableProvider<T> vProvider) {
        this(new UserKeys(), vProvider);
    }

    protected UserKeys uk() {
        return (UserKeys) keys;
    }

    @Override
    public T fromMap(ConversionMap<String, Object> map, T user) {
        user = super.fromMap(map, user);
        user.setEmail(map.getString(uk().email()));
        user.setFirstName(map.getString(uk().firstName()));
        user.setIdP(map.getString(uk().idp()));
        user.setIDPName(map.getString(uk().idpDisplayName()));
        user.setLastName(map.getString(uk().lastName()));
        user.setSerialIdentifier(newID(map.getString(uk().serialString())));
        UserMultiID userMultiKey = new UserMultiID(
                new RemoteUserName(map.getString(uk().remoteUser())),
                new EduPersonPrincipleName(map.getString(uk().eppn())),
                new EduPersonTargetedID(map.getString(uk().eptid())),
                new OpenID(map.getString(uk().openID())),
                new OpenIDConnect(map.getString(uk().oidc())),
                new PairwiseID(map.getString(uk().pairwiseId())),
                new SubjectID(map.getString(uk().subjectId())));
        user.setUserMultiKey(userMultiKey);
        user.setIdentifier(newID(map.getString(uk().userID())));
        user.setAffiliation(map.getString(uk().affiliation()));
        user.setDisplayName(map.getString(uk().displayName()));
        user.setOrganizationalUnit(map.getString(uk().organizationalUnit()));
        user.setUseUSinDN(map.getBoolean(uk().useUSinDN()));
        user.setAttr_json(map.getString(uk().attr_json()));
        user.setState(JSONObject.fromObject(map.getString(uk().state())));
        return user;
    }

    @Override
    public void toMap(T user, ConversionMap<String, Object> map) {
        super.toMap(user, map);
        map.put(uk().remoteUser(), replaceNull(user.getRemoteUser()));
        map.put(uk().eppn(), replaceNull(user.getePPN()));
        map.put(uk().eptid(), replaceNull(user.getePTID()));
        map.put(uk().openID(), replaceNull(user.getOpenID()));
        map.put(uk().oidc(), replaceNull(user.getOpenIDConnect()));
        map.put(uk().pairwiseId(), replaceNull(user.getPairwiseID()));
        map.put(uk().subjectId(), replaceNull(user.getSubjectID()));
        map.put(uk().userID(), user.getIdentifierString());
        map.put(uk().idp(), replaceNull(user.getIdP()));
        map.put(uk().idpDisplayName(), replaceNull(user.getIDPName()));
        map.put(uk().firstName(), replaceNull(user.getFirstName()));
        map.put(uk().lastName(), replaceNull(user.getLastName()));
        map.put(uk().email(), replaceNull(user.getEmail()));
        // Fixes for  https://github.com/cilogon/cilogon-java/issues/53
        if(user.getSerialIdentifier() != null) {
            map.put(uk().serialString(), user.getSerialIdentifier().toString());
        }
        map.put(uk().affiliation(), user.getAffiliation());
        map.put(uk().displayName(), user.getDisplayName());
        if(user.getOrganizationalUnit() != null) {
            map.put(uk().organizationalUnit(), user.getOrganizationalUnit());
        }

        map.put(uk().useUSinDN(), user.isUseUSinDN());
        if(user.getAttr_json() == null) {
            map.put(uk().attr_json(), ""); // CIL
        }else{

            map.put(uk().attr_json(), replaceNull(user.getAttr_json()));
        }
        if(user.getState() != null) {
            map.put(uk().state(), user.getState().toString());
        }
        System.err.println(getClass().getSimpleName() + ".toMap:\n" + map);
    }

    /**
     * Checks that the {@link PersonName} is not trivial. If it is, an
     * empty string is returned rather than a Java null. Otherwise, the
     * {@link PersonName#getName()} value is returned.
     * @param x
     * @return
     */
    protected String replaceNull(PersonName x) {
        if (x == null) {
            return "";
        }
        if (x.getName() == null) return "";
        return x.getName();
    }

    /**
     * If a null String is encountered, it is replaced with an empty string,
     * otherwise it is left alone.
     * @param x
     * @return
     */
    protected String replaceNull(String x) {
        if (x == null) {
            return "";
        }
        return x;
    }
}
