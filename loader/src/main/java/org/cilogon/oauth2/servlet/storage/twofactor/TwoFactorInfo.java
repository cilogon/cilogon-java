package org.cilogon.oauth2.servlet.storage.twofactor;

import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.util.IdentifiableImpl;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/18/12 at  9:20 AM
 */
public class TwoFactorInfo extends IdentifiableImpl {
    public TwoFactorInfo(Identifier identifier) {
        super(identifier);
    }

    public TwoFactorInfo(Identifier identifier, String info) {
        super(identifier);
        this.info = info;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    String info;

    @Override
    public String toString() {
        String tempInfo = getInfo();
        if (tempInfo == null || tempInfo.length() == 0) {
            tempInfo = "(null)";
        } else {
            int len = Math.min(25, tempInfo.length());
            tempInfo = tempInfo.substring(0, len) + (tempInfo.length() == len ? "" : "...");
        }
        return "TwoFactor[id=" + (getIdentifier() == null ? "(null)" : getIdentifierString()) + ", info=" + tempInfo + "]";
    }
}
