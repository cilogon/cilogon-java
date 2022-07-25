package org.cilogon.oauth2.servlet.storage.user;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/30/14 at  2:18 PM
 */
public class RemoteUserName extends PersonName {
    public RemoteUserName(String name) {
        super(name);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof RemoteUserName)) return false;
        return super.equals(obj);
    }
}
