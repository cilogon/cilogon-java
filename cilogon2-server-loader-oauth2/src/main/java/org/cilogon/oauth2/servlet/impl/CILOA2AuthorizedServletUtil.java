package org.cilogon.oauth2.servlet.impl;

import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.OA2ServiceTransaction;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.servlet.OA2AuthorizedServletUtil;
import edu.uiuc.ncsa.myproxy.oa4mp.server.servlet.MyProxyDelegationServlet;
import edu.uiuc.ncsa.security.delegation.token.AuthorizationGrant;
import org.cilogon.oauth2.servlet.storage.CILOA2ServiceTransaction;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 5/25/18 at  2:25 PM
 */
public class CILOA2AuthorizedServletUtil extends OA2AuthorizedServletUtil {
    public CILOA2AuthorizedServletUtil(MyProxyDelegationServlet servlet) {
        super(servlet);
    }

    @Override
    protected OA2ServiceTransaction createNewTransaction(AuthorizationGrant grant) {
        return new CILOA2ServiceTransaction(grant);
    }

}
