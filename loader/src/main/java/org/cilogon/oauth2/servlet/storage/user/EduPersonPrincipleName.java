package org.cilogon.oauth2.servlet.storage.user;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/30/14 at  2:19 PM
 */
public class EduPersonPrincipleName extends PersonName {
    public EduPersonPrincipleName(String name) {
        super(name);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof EduPersonPrincipleName)) return false;
        return super.equals(obj);
    }
}
