package org.cilogon.oauth2.servlet.impl;

import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.servlet.OA2ATServlet;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 2/2/17 at  1:37 PM
 */
public class CILOA2ATServlet extends OA2ATServlet {
/*
    @Override
    public LinkedList<ScopeHandler>  setupScopeHandlers(OA2ServiceTransaction transaction, OA2SE oa2SE) {
        LinkedList<ScopeHandler> scopeHandlers = new LinkedList<>();
        if(oa2SE.getScopeHandler()!= null && oa2SE.getScopeHandler().isEnabled()) {
            scopeHandlers.add(oa2SE.getScopeHandler());
        }
        OA2Client client = (OA2Client) transaction.getClient();
        DebugUtil.dbg(this, "Getting configured scope handler factory " + LDAPScopeHandlerFactory.getFactory().getClass().getSimpleName());
        scopeHandlers.addAll(LDAPScopeHandlerFactory.createScopeHandlers(oa2SE, client));
        return scopeHandlers;
    }

*/
    /*  @Override
    protected LinkedList<ScopeHandler> getScopeHandlers(OA2SE oa2SE, OA2Client client) {
        CILogonOA2ServiceEnvironment coa2se = (CILogonOA2ServiceEnvironment) oa2SE;

        LinkedList<ScopeHandler> scopeHandlers = new LinkedList<>();
      //  CILogonScopeHandler cil = CILOA2ScopeHandlerFactory.getFactory().create()

              if (client.getLdaps()==null || client.getLdaps().isEmpty()) {
                  DebugUtil.dbg(this, "using default scope handler=");
                  scopeHandlers.add(coa2se.getScopeHandler());
              } else {
                  // special treatment here is that there is always a CIL-OA2 scope handler.
                  for (LDAPConfiguration cfg : client.getLdaps()) {
                      LDAPScopeHandlerFactoryRequest req = new LDAPScopeHandlerFactoryRequest(getMyLogger(),
                              cfg, client.getScopes());
                      ScopeHandler scopeHandler = ScopeHandlerFactory.newInstance(req);
                      scopeHandlers.add(scopeHandler);
                  }
              }
              return scopeHandlers;
    }*/
}
