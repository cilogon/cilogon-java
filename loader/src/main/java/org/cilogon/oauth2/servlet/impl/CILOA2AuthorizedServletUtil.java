package org.cilogon.oauth2.servlet.impl;

import org.cilogon.oauth2.servlet.storage.transaction.CILOA2ServiceTransaction;
import org.oa4mp.delegation.common.token.AuthorizationGrant;
import org.oa4mp.server.api.storage.servlet.MyProxyDelegationServlet;
import org.oa4mp.server.loader.oauth2.servlet.OA2AuthorizedServletUtil;
import org.oa4mp.server.loader.oauth2.storage.transactions.OA2ServiceTransaction;

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
