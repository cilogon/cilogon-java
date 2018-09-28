package org.cilogon.d2.storage;

import edu.uiuc.ncsa.security.core.util.BeanUtils;

import java.io.Serializable;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/30/14 at  2:17 PM
 */
public abstract class PersonName implements Serializable {
    protected PersonName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    String name;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        PersonName pn = (PersonName) obj;
        if (!BeanUtils.checkNoNulls(getName(), pn.getName())) return false;
        return getName().equals(pn.getName());
    }

    @Override
    public String toString() {
        if (name == null && 0 < name.length()) {
            return "";
        }
        return name.toString();
    }

    public boolean isEmpty() {
        return name == null && name.isEmpty();
    }
}
