package org.cilogon.d2.util;

import edu.uiuc.ncsa.security.core.IdentifiableProvider;
import edu.uiuc.ncsa.security.storage.data.ConversionMap;
import edu.uiuc.ncsa.security.storage.data.MapConverter;
import org.cilogon.d2.storage.ArchivedUser;
import org.cilogon.d2.storage.ArchivedUserKeys;
import org.cilogon.d2.storage.User;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/16/12 at  5:10 PM
 */
public class ArchivedUserConverter extends MapConverter<ArchivedUser> {
    public ArchivedUserConverter(ArchivedUserKeys keys, IdentifiableProvider<ArchivedUser> vProvider,
                                 UserConverter umc) {
        super(keys, vProvider);
        userMapConverter = umc;
    }

    UserConverter userMapConverter;

    protected ArchivedUserKeys getAUKeys() {
        return (ArchivedUserKeys) keys;
    }

    @Override
    public ArchivedUser fromMap(ConversionMap<String, Object> map, ArchivedUser v) {
        ArchivedUser archivedUser = super.fromMap(map, v);
        User u = userMapConverter.fromMap(map, archivedUser.getUser());
        archivedUser.setArchivedDate(map.getDate(getAUKeys().archivedTimestampColumn()));
        archivedUser.setArchivedDate(map.getDate(getAUKeys().archivedTimestampColumn()));
        archivedUser.setUser(u);
        return archivedUser;
    }

    @Override
    public void toMap(ArchivedUser value, ConversionMap<String, Object> data) {
        super.toMap(value, data);
        data.put(getAUKeys().archivedTimestampColumn(), value.getArchivedDate());
        userMapConverter.toMap(value.getUser(), data);
    }
}
