package org.cilogon.oauth2.servlet;

import org.cilogon.oauth2.servlet.claims.CILCSConstants;
import org.cilogon.oauth2.servlet.claims.SAMLAttributeClaimSource;
import org.oa4mp.delegation.server.server.claims.ClaimSource;
import org.oa4mp.server.loader.oauth2.OA2SE;
import org.oa4mp.server.loader.qdl.claims.ConfigtoCS;
import org.qdl_lang.state.State;
import org.qdl_lang.variables.QDLStem;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 2/8/23 at  1:45 PM
 */
public class CILConfigToCS extends ConfigtoCS {
    @Override
    public ClaimSource convert(QDLStem arg, State qdlState, OA2SE oa2SE) {

        switch (arg.getString(CS_DEFAULT_TYPE)) {
            case CILCSConstants.CS_TYPE_SAML:
                return new SAMLAttributeClaimSource();
        }
        return super.convert(arg, qdlState, oa2SE);
    }
}
