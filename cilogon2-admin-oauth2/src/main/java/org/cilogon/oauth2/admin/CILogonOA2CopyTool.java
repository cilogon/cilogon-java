package org.cilogon.oauth2.admin;

import edu.uiuc.ncsa.myproxy.oauth2.tools.OA2CopyTool;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/25/15 at  3:37 PM
 */
public class CILogonOA2CopyTool extends OA2CopyTool {
    public CILogonOA2CopyTool() {
        setCopyExtension(new CILogonCopyExtension(this));
    }

    public static void main(String[] args) {
        // Standard override of the admin tool.
        CILogonOA2CopyTool adminTool = new CILogonOA2CopyTool();
           try {
               adminTool.run(args);
           } catch (Throwable e) {
               // Since this will probably be called only by a bash script, catch all errors and exceptions
               // then return a non-zero exit code
               e.printStackTrace();
               System.exit(1);
           }
       }
}
