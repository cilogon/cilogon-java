package org.cilogon.proxy.servlet;

import org.cilogon.oauth2.servlet.impl.CILOA2AuthorizedServletUtil;
import org.oa4mp.server.loader.oauth2.servlet.OA2AuthorizedServletUtil;
import org.oa4mp.server.proxy.OA2AuthorizationServer;

public class CILAuthorizationServer extends OA2AuthorizationServer {
    @Override
    protected OA2AuthorizedServletUtil getInitUtil() {
        return new CILOA2AuthorizedServletUtil(this);
    }
}
