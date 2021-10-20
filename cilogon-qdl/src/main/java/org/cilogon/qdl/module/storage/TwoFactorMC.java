package org.cilogon.qdl.module.storage;

import edu.uiuc.ncsa.oa2.qdl.storage.StemConverter;
import edu.uiuc.ncsa.qdl.variables.StemVariable;
import edu.uiuc.ncsa.security.storage.data.MapConverter;
import org.cilogon.d2.twofactor.TwoFactorInfo;
import org.cilogon.d2.twofactor.TwoFactorSerializationKeys;

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
    public V fromMap(StemVariable stem, V v) {
        v = super.fromMap(stem, v);
        if (isStringKeyOK(stem, kk().info())) {
            v.setInfo(stem.getString(kk().info()));
        }
        return v;
    }

    @Override
    public StemVariable toMap(V v, StemVariable stem) {
        stem = super.toMap(v, stem);
        if (!isTrivial(v.getInfo())) {
            stem.put(kk().info(), v.getInfo());
        }
        return stem;
    }
}
