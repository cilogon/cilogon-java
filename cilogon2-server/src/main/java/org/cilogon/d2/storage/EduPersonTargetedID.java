package org.cilogon.d2.storage;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/30/14 at  2:18 PM
 */
public class EduPersonTargetedID extends PersonName {
    public EduPersonTargetedID(String name) {
        super(name);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof EduPersonTargetedID)) return false;
        return super.equals(obj);
    }
}
