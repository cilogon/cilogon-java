package org.cilogon.oauth2.admin;

import edu.uiuc.ncsa.myproxy.oa4mp.server.CopyExtension;
import edu.uiuc.ncsa.myproxy.oa4mp.server.CopyTool;
import org.cilogon.oauth2.servlet.util.CILogonSE;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/25/15 at  3:13 PM
 */
public class CILogonCopyExtension extends CopyExtension {
    public CILogonCopyExtension(CopyTool copyTool) {
        this.copyTool = copyTool;
    }

    CopyTool copyTool;

    @Override
    public int copy(int totalRecs) {
        CILogonSE srcE = (CILogonSE) copyTool.getSourceEnv();
        CILogonSE trgE = (CILogonSE) copyTool.getTargetEnv();
        int currentRecCount = srcE.getIDPStore().size();
        totalRecs += currentRecCount;
        copyTool.sayv("Copying " + currentRecCount + " idps...");
        copyTool.wipeAndCopy(srcE.getIDPStore(), trgE.getIDPStore());


        currentRecCount = srcE.getUserStore().size();
        totalRecs += currentRecCount;
        copyTool.sayv("Copying " + currentRecCount + " users...");
        copyTool.wipeAndCopy(srcE.getUserStore(), trgE.getUserStore());

        currentRecCount = srcE.getTwoFactorStore().size();
        totalRecs += currentRecCount;
        copyTool.sayv("Copying " + currentRecCount + " two-factor information");
        copyTool.wipeAndCopy(srcE.getTwoFactorStore(), trgE.getTwoFactorStore());

        currentRecCount = srcE.getArchivedUserStore().size();
        totalRecs += currentRecCount;
        copyTool.sayv("Copying " + currentRecCount + " archived users...");
        copyTool.wipeAndCopy(srcE.getArchivedUserStore(), trgE.getArchivedUserStore());

        // Avoid using a lower value for incrementable. Always use larger value of the two.
        long nextValueSource = srcE.getIncrementable().nextValue();
        long nextValueTarget = trgE.getIncrementable().nextValue();
        // Most stores will return the initial value after a reset but the source will return the next, so
        // increment to stay in sync. Teh target should start up seamlessly after the source.
        long nextValue = 1 + Math.max(nextValueSource, nextValueTarget);
        copyTool.sayv("Copying incrementables, start value = " + nextValue);
        trgE.getIncrementable().destroy(); // destroy rather than init, since init creates a clean sequence.
        trgE.getIncrementable().createNew(nextValue);
        return totalRecs;
    }
}
