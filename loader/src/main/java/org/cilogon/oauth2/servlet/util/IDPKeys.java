package org.cilogon.oauth2.servlet.util;

import edu.uiuc.ncsa.security.storage.data.SerializationKeys;

/**
* <p>Created by Jeff Gaynor<br>
* on 4/26/12 at  10:04 AM
*/
public class IDPKeys extends SerializationKeys {
    public IDPKeys() {
        identifier("idp_uid");
    }
}
