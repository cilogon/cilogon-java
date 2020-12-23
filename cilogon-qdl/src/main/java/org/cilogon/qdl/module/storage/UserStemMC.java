package org.cilogon.qdl.module.storage;

import edu.uiuc.ncsa.oa2.qdl.storage.StemConverter;
import edu.uiuc.ncsa.qdl.variables.StemVariable;
import edu.uiuc.ncsa.security.storage.data.MapConverter;
import net.sf.json.JSONObject;
import org.cilogon.d2.storage.*;
import org.cilogon.d2.util.UserKeys;

import static edu.uiuc.ncsa.security.core.util.BasicIdentifier.newID;
import static edu.uiuc.ncsa.security.core.util.StringUtils.isTrivial;

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

    @Override
    public V fromMap(StemVariable stem, V v) {
        v = super.fromMap(stem, v);
        if (isStringKeyOK(stem, kk().attr_json())) {
            v.setAttr_json(stem.getString(kk().attr_json()));
        }
        if (stem.containsKey(kk().creationTimestamp())) {
            v.setCreationTime(toDate(stem, kk().creationTimestamp()));
        }
        if (isStringKeyOK(stem, kk().email())) {
            v.setEmail(stem.getString(kk().email()));
        }
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
        if (isStringKeyOK(stem, kk().firstName())) {
            v.setFirstName(stem.getString(kk().firstName()));
            ;
        }
        if (isStringKeyOK(stem, kk().idp())) {
            v.setIdP(stem.getString(kk().idp()));
        }
        if (isStringKeyOK(stem, kk().idpDisplayName())) {
            v.setIDPName(stem.getString(kk().idpDisplayName()));
        }
        if (isStringKeyOK(stem, kk().lastName())) {
            v.setLastName(stem.getString(kk().lastName()));
        }
        if (isStringKeyOK(stem, kk().serialString())) {
            v.setSerialIdentifier(newID(stem.getString(kk().serialString())));
        }
        if (isStringKeyOK(stem, kk().state())) {
            v.setState(JSONObject.fromObject(stem.getString(kk().state())));
        }
        v.setUseUSinDN(stem.getBoolean(kk().useUSinDN()));
        if(isStringKeyOK(stem, kk().organizationalUnit())){
            v.setOrganizationalUnit(stem.getString(kk().organizationalUnit()));
        }
        if(isStringKeyOK(stem, kk().displayName())){
            v.setDisplayName(stem.getString(kk().displayName()));
        }
        if(isStringKeyOK(stem, kk().affiliation())){
            v.setAffiliation(stem.getString(kk().affiliation()));
        }
        return v;
    }

    @Override
    public StemVariable toMap(V v, StemVariable stem) {
        stem = super.toMap(v, stem);
        if (!isTrivial(v.getAttr_json())) {
            stem.put(kk().attr_json(), v.getAttr_json());
        }
        stem.put(kk().creationTimestamp(), v.getCreationTime().getTime());
        if (!isTrivial(kk().email())) {
            stem.put(kk().email(), v.getEmail());
        }
        if (v.getePPN() != null) {
            stem.put(kk().eppn(), v.getePPN().getName());
        }
        if (v.getePTID() != null) {
            stem.put(kk().eptid(), v.getePTID().getName());
        }
        if (!isTrivial(v.getFirstName())) {
            stem.put(kk().firstName(), v.getFirstName());
        }
        if (!isTrivial(v.getIdP())) {
            stem.put(kk().idp(), v.getIdP());
        }
        if (!isTrivial(v.getIDPName())) {
            stem.put(kk().idpDisplayName(), v.getIDPName());
        }
        if (!isTrivial(v.getLastName())) {
            stem.put(kk().lastName(), v.getLastName());
        }
        if (v.getSerialIdentifier() != null) {
            stem.put(kk().serialString(), v.getSerialIdentifier().toString());
        }
        if (v.getOpenIDConnect() != null) {
            stem.put(kk().oidc(), v.getOpenIDConnect().getName());
        }
        if (v.getOpenID() != null) {
            stem.put(kk().openID(), v.getOpenID().getName());
        }
        if (v.getRemoteUser() != null) {
            stem.put(kk().remoteUser(), v.getRemoteUser().getName());
        }
        if (v.getSerialString() != null) {
            stem.put(kk().serialString(), v.getSerialString());
        }
        if (v.getState() != null) {
            stem.put(kk().state(), v.getState().toString());
        }
        stem.put(kk().useUSinDN(), v.isUseUSinDN());
        if (v.getPairwiseID() != null) {
            stem.put(kk().pairwiseId(), v.getPairwiseID().getName());
        }
        if (v.getSubjectID() != null) {
            stem.put(kk().subjectId(), v.getSubjectID().getName());
        }
        if (!isTrivial(v.getOrganizationalUnit())) {
            stem.put(kk().organizationalUnit(), v.getOrganizationalUnit());
        }
        if (!isTrivial(v.getDisplayName())) {
            stem.put(kk().displayName(), v.getDisplayName());
        }
        if (!isTrivial(v.getAffiliation())) {
            stem.put(kk().affiliation(), v.getAffiliation());
        }
        return stem;
    }


}
