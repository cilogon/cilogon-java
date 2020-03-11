package org.cilogon.d2.util;

import edu.uiuc.ncsa.security.core.IdentifiableProvider;
import edu.uiuc.ncsa.security.storage.data.ConversionMap;
import edu.uiuc.ncsa.security.storage.data.MapConverter;
import edu.uiuc.ncsa.security.storage.data.SerializationKeys;
import net.sf.json.JSONObject;
import org.cilogon.d2.storage.*;

import static edu.uiuc.ncsa.security.core.util.BasicIdentifier.newID;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/16/12 at  5:34 PM
 */
public class UserConverter<T extends User> extends MapConverter<T> {
    public UserConverter(SerializationKeys keys, IdentifiableProvider<T> vProvider) {
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
                new OpenIDConnect(map.getString(uk().oidc())));
        user.setUserMultiKey(userMultiKey);
        user.setCreationTime(map.getDate(uk().creationTimestamp()));
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
        map.put(uk().userID(), user.getIdentifierString());
        map.put(uk().idp(), replaceNull(user.getIdP()));
        map.put(uk().idpDisplayName(), replaceNull(user.getIDPName()));
        map.put(uk().firstName(), replaceNull(user.getFirstName()));
        map.put(uk().lastName(), replaceNull(user.getLastName()));
        map.put(uk().email(), replaceNull(user.getEmail()));
        map.put(uk().serialString(), user.getSerialIdentifier().toString());
        map.put(uk().creationTimestamp(), user.getCreationTime());
        map.put(uk().affiliation(), user.getAffiliation());
        map.put(uk().displayName(), user.getDisplayName());
        map.put(uk().organizationalUnit(), user.getOrganizationalUnit());
        map.put(uk().useUSinDN(), user.isUseUSinDN());
        map.put(uk().attr_json(), replaceNull(user.getAttr_json()));
        map.put(uk().state(), user.getState().toString());
    }

    protected String replaceNull(PersonName x) {
        if (x == null) {
            return "";
        }
        if (x.getName() == null) return "";
        return x.getName();
    }

    protected String replaceNull(String x) {
        if (x == null) {
            return "";
        }
        return x;
    }
}
