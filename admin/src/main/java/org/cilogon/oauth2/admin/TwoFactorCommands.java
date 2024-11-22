package org.cilogon.oauth2.admin;

import edu.uiuc.ncsa.security.core.Identifiable;
import edu.uiuc.ncsa.security.core.Store;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import org.cilogon.oauth2.servlet.storage.twofactor.TwoFactorInfo;
import org.cilogon.oauth2.servlet.storage.twofactor.TwoFactorSerializationKeys;
import org.cilogon.oauth2.servlet.storage.twofactor.TwoFactorStore;
import org.oa4mp.server.admin.myproxy.oauth2.base.StoreCommands2;

import java.io.IOException;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 11/6/13 at  1:40 PM
 */
public class TwoFactorCommands extends StoreCommands2 {


    public TwoFactorCommands(MyLoggingFacade logger, String defaultIndent, Store store) throws Throwable{
        super(logger, defaultIndent, store);
    }

    public TwoFactorCommands(MyLoggingFacade logger, Store store) throws Throwable {
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
        return "two factor";
    }

    @Override
    public void extraUpdates(Identifiable identifiable, int magicNumber) throws IOException {
        super.extraUpdates(identifiable, magicNumber);
        TwoFactorInfo tfi = (TwoFactorInfo) identifiable;
        TwoFactorSerializationKeys keys = (TwoFactorSerializationKeys) getSerializationKeys();
        String defaultInfo = tfi.getInfo();
        if (!isEmpty(defaultInfo)){
            int len = Math.min(25, defaultInfo.length());
            defaultInfo = defaultInfo.substring(0, len) + (defaultInfo.length() == len ? "" : "...");
        }

        String newInfo = getPropertyHelp(keys.info(), "Enter new info", defaultInfo);
        if (!newInfo.equals(defaultInfo)) {
            tfi.setInfo(newInfo);
        }

    }

/*
    @Override
    public boolean update(Identifiable identifiable) throws IOException {
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
    }
*/


}
