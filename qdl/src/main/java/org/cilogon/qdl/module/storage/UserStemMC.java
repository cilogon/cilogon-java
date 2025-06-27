package org.cilogon.qdl.module.storage;

import edu.uiuc.ncsa.security.storage.data.MapConverter;
import net.sf.json.JSONObject;
import org.cilogon.oauth2.servlet.storage.user.*;
import org.oa4mp.server.qdl.storage.StemConverter;
import org.qdl_lang.variables.QDLStem;

import static edu.uiuc.ncsa.security.core.util.BasicIdentifier.newID;
import static org.qdl_lang.variables.StemUtility.put;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 12/22/20 at  2:54 PM
 */
public class UserStemMC<V extends User> extends StemConverter<V> {
    public UserStemMC(MapConverter<V> mapConverter) {
        super(mapConverter);
    }


    UserKeys kk() {
        return (UserKeys) keys;
    }
    /*
    public String affiliation = "affiliation";
    public String attr_json = "attr_json";
    public String creationTimestamp = "create_time";
    public String displayName = "display_name";
    public String email = "email";
    public String eppn = "eppn";
    public String eptid = "eptid";
    public String firstName = "first_name";
    public String idp = "idp";
    public String idpDisplayName = "idp_display_name";
    public String lastName = "last_name";
    public String oidc = "oidc";
    public String openID = "open_id";
    public String organizationalUnit = "ou";
    public String pairwiseid = "pairwise_id";
    public String remoteUser = "remote_user";
    public String serialString = "serial_string";
    public String state = "state";
    public String subjectid = "subject_id";
    public String userID = "user_uid";
    public String useUSinDN = "us_idp";
     */


    @Override
    public V fromMap(QDLStem stem, V v) {
        v = super.fromMap(stem, v);
        if(isStringKeyOK(stem, kk().affiliation())){v.setAffiliation(stem.getString(kk().affiliation()));}
        if (isStringKeyOK(stem, kk().attr_json())) {v.setAttr_json(stem.getString(kk().attr_json()));}
        if (stem.containsKey(kk().lastAccessed())) {v.setLastAccessed(toDate(stem, kk().lastAccessed()));}
        if (stem.containsKey(kk().lastModifiedTS())) {v.setLastModifiedTS(toDate(stem, kk().lastModifiedTS()));}
        if (stem.containsKey(kk().creationTS())) {v.setCreationTS(toDate(stem, kk().creationTS()));}
        if(isStringKeyOK(stem, kk().displayName())){v.setDisplayName(stem.getString(kk().displayName()));}
        if (isStringKeyOK(stem, kk().email())) {v.setEmail(stem.getString(kk().email()));}
        // NOTE the eppn, eptid, etc. actually all reside in the user multi-key.
        // This has to be created and assembled. Each of the components is stored
        // separately in the store though.
        UserMultiID userMultiKey = new UserMultiID(
                new RemoteUserName(stem.getString(kk().remoteUser())),
                new EduPersonPrincipleName(stem.getString(kk().eppn())),
                new EduPersonTargetedID(stem.getString(kk().eptid())),
                new OpenID(stem.getString(kk().openID())),
                new OpenIDConnect(stem.getString(kk().oidc())),
                new PairwiseID(stem.getString(kk().pairwiseId())),
                new SubjectID(stem.getString(kk().subjectId())));
        v.setUserMultiKey(userMultiKey);
        if (isStringKeyOK(stem, kk().firstName())) {v.setFirstName(stem.getString(kk().firstName()));}
        if (isStringKeyOK(stem, kk().idp())) {v.setIdP(stem.getString(kk().idp()));}
        if (isStringKeyOK(stem, kk().idpDisplayName())) {v.setIDPName(stem.getString(kk().idpDisplayName()));}
        if (isStringKeyOK(stem, kk().lastName())) {v.setLastName(stem.getString(kk().lastName()));}
        if(isStringKeyOK(stem, kk().organizationalUnit())){v.setOrganizationalUnit(stem.getString(kk().organizationalUnit()));}
        if (isStringKeyOK(stem, kk().serialString())) {v.setSerialIdentifier(newID(stem.getString(kk().serialString())));}

        if (isStringKeyOK(stem, kk().state())) {
            QDLStem QDLStem = stem.get(kk().state()).asStem();
            v.setState((JSONObject) QDLStem.toJSON());
        }
        v.setUseUSinDN(stem.getBoolean(kk().useUSinDN()));
        return v;
    }
    @Override
    public QDLStem toMap(V v, QDLStem stem) {
        stem = super.toMap(v, stem);
        setNonNullStemValue(stem,kk().affiliation(), v.getAffiliation());
        if(v.getLastAccessed() != null){
            put(stem,kk().lastAccessed(), v.getLastAccessed().getTime());
        }
        if(v.getLastModifiedTS() != null){
            put(stem,kk().lastModifiedTS(), v.getLastModifiedTS().getTime());
        }

        setNonNullStemValue(stem,kk().attr_json(), v.getAttr_json());
        put(stem,kk().creationTS(), v.getCreationTS().getTime());
        setNonNullStemValue(stem,kk().idpDisplayName(), v.getIDPName());
        setNonNullStemValue(stem,kk().email(), v.getEmail());
        if (v.getePPN() != null) {put(stem,kk().eppn(), v.getePPN().getName());}
        if (v.getePTID() != null) {put(stem,kk().eptid(), v.getePTID().getName());}
        setNonNullStemValue(stem,kk().firstName(), v.getFirstName());
        setNonNullStemValue(stem,kk().idp(), v.getIdP());
        setNonNullStemValue(stem,kk().displayName(), v.getDisplayName());
        setNonNullStemValue(stem,kk().lastName(), v.getLastName());
        if (v.getOpenIDConnect() != null) {put(stem,kk().oidc(), v.getOpenIDConnect().getName());}
        if (v.getOpenID() != null) {put(stem,kk().openID(), v.getOpenID().getName());}
        setNonNullStemValue(stem,kk().organizationalUnit(), v.getOrganizationalUnit());
        if (v.getPairwiseID() != null) {put(stem,kk().pairwiseId(), v.getPairwiseID().getName());}
        if (v.getRemoteUser() != null) {put(stem,kk().remoteUser(), v.getRemoteUser().getName());}
        if (v.getSerialIdentifier() != null) {put(stem,kk().serialString(), v.getSerialIdentifier().toString());}
        //    setNonNullStemValue(stem,kk().serialString(), v.getSerialString());
        if (v.getState() != null) {
            QDLStem QDLStem = new QDLStem();
            QDLStem.fromJSON(v.getState());
            put(stem,kk().state(), QDLStem);
        }
        if (v.getSubjectID() != null) {put(stem,kk().subjectId(), v.getSubjectID().getName());}
         // User UID is the identifier for htis object and is set in super.
        put(stem,kk().useUSinDN(), v.isUseUSinDN());
        return stem;
    }

}
