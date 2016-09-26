package org.cilogon.d2.twofactor;

import edu.uiuc.ncsa.security.storage.data.ConversionMap;
import edu.uiuc.ncsa.security.storage.data.MapConverter;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/18/12 at  9:33 AM
 */
public class TwoFactorMapConverter extends MapConverter<TwoFactorInfo> {
    public TwoFactorMapConverter(TwoFactorInfoProvider twoFactorInfoProvider) {
        super(new TwoFactorSerializationKeys(), twoFactorInfoProvider);
    }

    protected TwoFactorSerializationKeys tfKeys() {
        return (TwoFactorSerializationKeys) keys;
    }

    @Override
    public TwoFactorInfo fromMap(ConversionMap<String, Object> map, TwoFactorInfo tfi) {
        tfi = super.fromMap(map, tfi);
        tfi.setInfo(map.getString(tfKeys().info()));
        return tfi;
    }

    @Override
    public void toMap(TwoFactorInfo tfi, ConversionMap<String, Object> map) {
        super.toMap(tfi, map);
        map.put(tfKeys().info(), tfi.getInfo());
    }
}
