package org.cilogon.oauth2.servlet.impl;

import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.servlet.OA2AuthorizedServlet;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.servlet.OA2AuthorizedServletUtil;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/15/15 at  10:10 AM
 */
public class CILOA2AuthorizedServlet extends OA2AuthorizedServlet {
    @Override
    public OA2AuthorizedServletUtil getInitUtil() {
        return new CILOA2AuthorizedServletUtil(this);
    }
}
