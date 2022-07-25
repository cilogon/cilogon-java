package org.cilogon.oauth2.servlet.twofactor;

import edu.uiuc.ncsa.security.core.Store;
import edu.uiuc.ncsa.security.storage.data.MapConverter;

/**
 * Marker interface required by storage API.
 * <p>Created by Jeff Gaynor<br>
 * on 10/18/12 at  11:05 AM
 */
public interface TwoFactorStore extends Store<TwoFactorInfo> {
    MapConverter getMapConverter();
}
