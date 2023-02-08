package org.cilogon.oauth2.servlet;

import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.OA2SE;
import edu.uiuc.ncsa.myproxy.oa4mp.qdl.claims.ConfigtoCS;
import edu.uiuc.ncsa.oa4mp.delegation.oa2.server.claims.ClaimSource;
import edu.uiuc.ncsa.qdl.state.State;
import edu.uiuc.ncsa.qdl.variables.QDLStem;
import org.cilogon.oauth2.servlet.claims.CILCSConstants;
import org.cilogon.oauth2.servlet.claims.SAMLAttributeClaimSource;

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
