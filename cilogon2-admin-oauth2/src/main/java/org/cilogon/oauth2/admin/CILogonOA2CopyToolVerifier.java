package org.cilogon.oauth2.admin;

import edu.uiuc.ncsa.myproxy.oa4mp.server.CopyTool;
import edu.uiuc.ncsa.myproxy.oauth2.tools.OA2CopyToolVerifier;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/25/15 at  3:51 PM
 */
public class CILogonOA2CopyToolVerifier extends OA2CopyToolVerifier {

    @Override
     public CopyTool getCopyTool() {
         if (copyTool == null) {
             copyTool = new CILogonOA2CopyTool();
         }
         return copyTool;
     }

     public static void main(String[] args) {
         CILogonOA2CopyToolVerifier cctv = new CILogonOA2CopyToolVerifier();
            if (args == null || args.length == 0) {
                cctv.printHelp();
                return;
            }
            cctv.doIt(cctv.getCopyTool(), args);
        }
}
