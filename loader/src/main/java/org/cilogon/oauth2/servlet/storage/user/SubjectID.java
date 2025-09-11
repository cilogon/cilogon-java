package org.cilogon.oauth2.servlet.storage.user;

import edu.uiuc.ncsa.security.core.util.BeanUtils;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/17/20 at  12:56 PM
 */
public class SubjectID extends PersonName {
    public SubjectID(String name) {
        super(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        PersonName pn = (PersonName) obj;
        // Fix https://jira.ncsa.illinois.edu/browse/CIL-2299
        // This is per the spec for subject-id
        return BeanUtils.checkNoNulls(getName(), pn.getName(), true);
    }
}
