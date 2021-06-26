package org.cilogon.d2.servlet;

import edu.uiuc.ncsa.myproxy.oa4mp.server.servlet.MyProxyDelegationServlet;
import edu.uiuc.ncsa.myproxy.oa4mp.server.servlet.OA4MPServletInitializer;
import edu.uiuc.ncsa.security.core.util.DebugUtil;
import org.cilogon.d2.util.CILogonSE;
import org.cilogon.d2.util.DNUtil;

import javax.servlet.ServletException;
import java.io.IOException;
import java.sql.SQLException;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 11/30/15 at  1:44 PM
 */
public class CILServletInitializer extends OA4MPServletInitializer {
    @Override
    public void init() throws ServletException {
        MyProxyDelegationServlet mps = (MyProxyDelegationServlet) getServlet();
          try {
              // for production CILogon.
              CILogonSE se = (CILogonSE)getEnvironment();
              DebugUtil.setPrintTS(se.isPrintTSInDebug());
              mps.processStoreCheck(se.getUserStore());
              mps.processStoreCheck(se.getArchivedUserStore());
              mps.processStoreCheck(se.getIDPStore());
              mps.processStoreCheck(se.getTwoFactorStore());
              mps.storeUpdates();
              DNUtil.setComputeFNAL(se.isComputeFNAL());

          } catch (IOException | SQLException e) {
              e.printStackTrace();
              throw new ServletException("Could not update table", e);
          }
        super.init();
    }
}
