package org.cilogon.oauth2.servlet.twofactor;

import edu.uiuc.ncsa.security.storage.data.SerializationKeys;

import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/18/12 at  9:25 AM
 */
public class TwoFactorSerializationKeys extends SerializationKeys {
    public TwoFactorSerializationKeys() {
        identifier("user_uid");
    }

    String info = "two_factor";

     public String info(String... x) {
        if (0 < x.length) info = x[0];
        return info;
    }
    @Override
    public List<String> allKeys(){
        List<String> allKeys = super.allKeys();
        allKeys.add(info());
        return allKeys;
    }
}
