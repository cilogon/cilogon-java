package org.cilogon.oauth1.admin;

import edu.uiuc.ncsa.myproxy.oa4mp.server.CopyTool;
import edu.uiuc.ncsa.myproxy.oa4mp.server.CopyToolVerifier;
import edu.uiuc.ncsa.myproxy.oa4mp.server.ServiceEnvironmentImpl;
import org.cilogon.d2.util.CILogonSE;

/**
 * <h1>Caution!</h1>
 * This is a command line tool that will run the copy command (its arguments are the same)
 * then verify that <b>every</b> entry in each store is identical. As such, this might take a
 * long time to run. <b>YOU DO NOT WANT TO RUN THIS ON A PRODUCTION MACHINE.</b>
 * It is intended to be used as part of testing that the copy command works.
 * <p>Created by Jeff Gaynor<br>
 * on 11/6/13 at  11:24 AM
 */
public class CILogonCopyToolVerifier extends CopyToolVerifier {

    @Override
    public CopyTool getCopyTool() {
        if(copyTool == null){
            copyTool = new CILogonCopyTool();
        }
        return copyTool;
    }

    public static void main(String[] args) {
        CILogonCopyToolVerifier cctv = new CILogonCopyToolVerifier();
        if (args == null || args.length == 0) {
            cctv.printHelp();
            return;
        }
        cctv.doIt(cctv.getCopyTool(), args);
    }

    @Override
    public boolean verifyStores(ServiceEnvironmentImpl sEnv, ServiceEnvironmentImpl tEnv) {
        if (!super.verifyStores(sEnv, tEnv)) return false;
        CILogonSE csEnv = (CILogonSE) sEnv;
        CILogonSE ctEnv = (CILogonSE) tEnv;
        if (!verifyStore("users", csEnv.getUserStore(), ctEnv.getUserStore())) return false;
        if (!verifyStore("archived users", csEnv.getArchivedUserStore(), ctEnv.getArchivedUserStore())) return false;
        if (!verifyStore("idps", csEnv.getIDPStore(), ctEnv.getIDPStore())) return false;
        if (!verifyStore("two factor", csEnv.getTwoFactorStore(), ctEnv.getTwoFactorStore())) return false;
        saynoCR("Checking counters... ");
        long sNV = csEnv.getIncrementable().nextValue();
        long tNV = ctEnv.getIncrementable().nextValue();
        if (sNV != tNV) {
            say("warning: Source counter next value is " + sNV + " but target counter next value is " + tNV);
            say("         This is " + ((sNV < tNV)?"fine.":"not good."));
            return false;
        }
        say("ok!");
        return true;
    }


    public void printHelp() {
        say("CILogon copy tool verifier.");
        say("\njava -jar cilogon2-cp-verifier [args]\n");
        say("Where the arguments are identical to what you would supply in the copy tool.");
        say("This tool will execute a full copy of the source store to the target store.");
        say("It will then check that each copied store is identical to the source.");
        say("This is designed to be a complete and low-level check that looks at *every*");
        say("single entry in both stores and compares them. It is therefore not designed to be a standard tool");
        say("but is useful mostly for debugging the CILogonCopyTool command. You could also use it to ");
        say("copy a store to a file store or memory store to check that the copy tool works.");
        say("For large stores this is very slow and you should have a good reason for running this tool...");
    }
}
