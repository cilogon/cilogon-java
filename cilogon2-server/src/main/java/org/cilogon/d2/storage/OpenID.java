package org.cilogon.d2.storage;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/30/14 at  2:22 PM
 */
public class OpenID extends PersonName {
    public OpenID(String name) {
        super(name);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof OpenID)) return false;
        return super.equals(obj);
    }
}
