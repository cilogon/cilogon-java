package org.cilogon.oauth2.servlet.impl;


import org.oa4mp.server.loader.oauth2.servlet.OA2AuthorizedServlet;
import org.oa4mp.server.loader.oauth2.servlet.OA2AuthorizedServletUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/15/15 at  10:10 AM
 */
public class CILOA2AuthorizedServlet extends OA2AuthorizedServlet {
    @Override
    public OA2AuthorizedServletUtil getInitUtil() {
        return new CILOA2AuthorizedServletUtil(this);
    }

    @Override
    protected void doIt(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Throwable {
        super.doIt(httpServletRequest, httpServletResponse);


    }
}
