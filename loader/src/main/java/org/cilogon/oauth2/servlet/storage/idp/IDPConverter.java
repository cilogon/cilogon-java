package org.cilogon.oauth2.servlet.storage.idp;

import edu.uiuc.ncsa.security.core.IdentifiableProvider;
import edu.uiuc.ncsa.security.storage.data.ConversionMap;
import edu.uiuc.ncsa.security.storage.monitored.MonitoredConverter;
import edu.uiuc.ncsa.security.storage.monitored.MonitoredKeys;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/16/12 at  5:00 PM
 */
public class IDPConverter extends MonitoredConverter<IdentityProvider> {
    public IDPConverter(IdentifiableProvider<IdentityProvider> identityProvider) {
        this(new IDPKeys(), identityProvider);

    }

    public IDPConverter(MonitoredKeys keys, IdentifiableProvider<IdentityProvider> identityProviderProvider) {
        super(keys, identityProviderProvider);
    }

    @Override
    public IdentityProvider fromMap(ConversionMap<String, Object> map, IdentityProvider identityProvider) {
        return super.fromMap(map, identityProvider);
    }

    @Override
    public void toMap(IdentityProvider value, ConversionMap<String, Object> data) {
        super.toMap(value, data);
    }
}
