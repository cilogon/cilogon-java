package org.cilogon.d2.util;

import edu.uiuc.ncsa.security.storage.data.SerializationKeys;

import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/26/12 at  10:03 AM
 */
public class UserKeys extends SerializationKeys {
    public UserKeys() {
        identifier(userID);
    }

    public String attr_json = "attr_json";
    public String creationTimestamp = "create_time";
    public String email = "email";
    public String eppn = "eppn";
    public String eptid = "eptid";
    public String firstName = "first_name";
    public String idp = "idp";
    public String idpDisplayName = "idp_display_name";
    public String lastName = "last_name";
    public String oidc = "oidc";
    public String openID = "open_id";
    public String remoteUser = "remote_user";
    public String serialString = "serial_string";
    public String state = "state";
    public String userID = "user_uid";
    public String useUSinDN = "us_idp";
    public String pairwiseid = "pairwise_id";
    public String subjectid = "subject_id";

    public String pairwiseId(String... x) {
        if (0 < x.length) pairwiseid = x[0];
        return pairwiseid;
    }


    public String subjectId(String... x) {
        if (0 < x.length) subjectid = x[0];
        return subjectid;
    }

    public String useUSinDN(String... x) {
        if (0 < x.length) useUSinDN = x[0];
        return useUSinDN;
    }

    public String state(String... x) {
        if (0 < x.length) state = x[0];
        return state;
    }

    public String attr_json(String... x) {
        if (0 < x.length) attr_json = x[0];
        return attr_json;
    }


    public String creationTimestamp(String... x) {
        if (0 < x.length) creationTimestamp = x[0];
        return creationTimestamp;
    }

    public String email(String... x) {
        if (0 < x.length) email = x[0];
        return email;
    }

    public String eppn(String... x) {
        if (0 < x.length) eppn = x[0];
        return eppn;
    }

    public String eptid(String... x) {
        if (0 < x.length) eptid = x[0];
        return eptid;
    }

    public String openID(String... x) {
        if (0 < x.length) openID = x[0];
        return openID;
    }

    public String oidc(String... x) {
        if (0 < x.length) oidc = x[0];
        return oidc;
    }

    public String firstName(String... x) {
        if (0 < x.length) firstName = x[0];
        return firstName;
    }

    public String idp(String... x) {
        if (0 < x.length) idp = x[0];
        return idp;
    }

    String affiliation = "affiliation";

    public String affiliation(String... x) {
        if (0 < x.length) affiliation = x[0];
        return affiliation;
    }

    public String organizationalUnit(String... x) {
        if (0 < x.length) organizationalUnit = x[0];
        return organizationalUnit;
    }

    String organizationalUnit = "ou";
    String displayName = "display_name";

    public String displayName(String... x) {
        if (0 < x.length) displayName = x[0];
        return displayName;
    }

    public String lastName(String... x) {
        if (0 < x.length) lastName = x[0];
        return lastName;
    }

    public String idpDisplayName(String... x) {
        if (0 < x.length) idpDisplayName = x[0];
        return idpDisplayName;
    }

    public String remoteUser(String... x) {
        if (0 < x.length) remoteUser = x[0];
        return remoteUser;
    }

    public String serialString(String... x) {
        if (0 < x.length) serialString = x[0];
        return serialString;
    }

    public String userID(String... x) {
        if (0 < x.length) userID = x[0];
        return userID;
    }

    @Override
    public List<String> allKeys() {
        List<String> allKeys = super.allKeys();
        allKeys.add(email());
        allKeys.add(firstName());
        allKeys.add(idp());
        allKeys.add(lastName());
        allKeys.add(idpDisplayName());
        allKeys.add(remoteUser());
        allKeys.add(serialString());
        allKeys.add(userID());
        allKeys.add(eppn());
        allKeys.add(eptid());
        allKeys.add(openID());
        allKeys.add(oidc());
        allKeys.add(useUSinDN());
        return allKeys;
    }
}
