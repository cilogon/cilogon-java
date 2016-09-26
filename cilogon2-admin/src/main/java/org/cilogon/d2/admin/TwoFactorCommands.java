package org.cilogon.d2.admin;

import edu.uiuc.ncsa.security.core.Identifiable;
import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.Store;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import edu.uiuc.ncsa.security.util.cli.StoreCommands;
import org.cilogon.d2.twofactor.TwoFactorInfo;
import org.cilogon.d2.twofactor.TwoFactorStore;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 11/6/13 at  1:40 PM
 */
public class TwoFactorCommands extends StoreCommands {
    @Override
    public void extraUpdates(Identifiable identifiable) {
    }

    public TwoFactorCommands(MyLoggingFacade logger, String defaultIndent, Store store) {
        super(logger, defaultIndent, store);
    }

    public TwoFactorCommands(MyLoggingFacade logger, Store store) {
        super(logger, store);
    }

    protected TwoFactorStore getTFStore() {
        return (TwoFactorStore) getStore();
    }

    @Override
    protected String format(Identifiable identifiable) {
        TwoFactorInfo tfi = (TwoFactorInfo) identifiable;
        return tfi.toString();
    }

    @Override
    public String getName() {
        return "  two factor";
    }

    @Override
    public boolean update(Identifiable identifiable) {
        boolean saveIt = false;
        TwoFactorInfo tfi = (TwoFactorInfo) identifiable;
        String currentID = tfi.getIdentifierString();
        info("Updating two-factor information for id=" + tfi.getIdentifierString());
        String newID = getInput("Enter new id", currentID);

        String defaultInfo = tfi.getInfo();
        if (defaultInfo == null || defaultInfo.length() == 0) {
            defaultInfo = "(null)";
        } else {
            int len = Math.min(25, defaultInfo.length());
            defaultInfo = defaultInfo.substring(0, len) + (defaultInfo.length() == len ? "" : "...");
        }

        String newInfo = getInput("Enter new info", defaultInfo);
        if (!newInfo.equals(defaultInfo)) {
            tfi.setInfo(newInfo);
            saveIt = true;
        }
        sayi("save changes [y/n]?");
        saveIt = isOk(readline());
        if (saveIt) {
            if (!currentID.equals(newID)) {
                Identifier id2 = BasicIdentifier.newID(newID);
                info("New two-factor id of " + newID + " does not match current ID, removing it to create a new one with the given id");
                getTFStore().remove(id2);
                info("Two-factor entry removed.");
                tfi.setIdentifier(id2);
            }

        }
        return saveIt;
 /*       if (saveIt) {
            getTFStore().save(tfi);
            info("Saved two-factor info for id=" + tfi.getIdentifierString());
            sayi("Two factor information updated.");
        }else{
            info("Did not save two-factor info for id=" + tfi.getIdentifierString());
        }
 */
    }

    @Override
    protected void longFormat(Identifiable identifiable) {
        TwoFactorInfo tfi = (TwoFactorInfo) identifiable;
        sayi("object id:" + tfi.getIdentifierString());
        sayi("     info:" + (tfi.getInfo() == null ? "(null)" : tfi.getInfo()));
    }
}
