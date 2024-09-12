package org.cilogon.qdl.module.storage;

import edu.uiuc.ncsa.security.storage.data.MapConverter;
import org.cilogon.oauth2.servlet.storage.twofactor.TwoFactorInfo;
import org.cilogon.oauth2.servlet.storage.twofactor.TwoFactorSerializationKeys;
import org.oa4mp.server.qdl.storage.StemConverter;
import org.qdl_lang.variables.QDLStem;

import static edu.uiuc.ncsa.security.core.util.StringUtils.isTrivial;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 12/23/20 at  1:25 PM
 */
public class TwoFactorMC<V extends TwoFactorInfo> extends StemConverter<V> {
    public TwoFactorMC(MapConverter<V> mapConverter) {
        super(mapConverter);
    }

    protected TwoFactorSerializationKeys kk() {
        return (TwoFactorSerializationKeys) keys;
    }

    @Override
    public V fromMap(QDLStem stem, V v) {
        v = super.fromMap(stem, v);
        if (isStringKeyOK(stem, kk().info())) {
            v.setInfo(stem.getString(kk().info()));
        }
        return v;
    }

    @Override
    public QDLStem toMap(V v, QDLStem stem) {
        stem = super.toMap(v, stem);
        if (!isTrivial(v.getInfo())) {
            stem.put(kk().info(), v.getInfo());
        }
        return stem;
    }
}
