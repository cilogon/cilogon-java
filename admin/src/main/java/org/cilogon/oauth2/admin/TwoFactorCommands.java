package org.cilogon.oauth2.admin;

import edu.uiuc.ncsa.security.core.Identifiable;
import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.Store;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import edu.uiuc.ncsa.security.util.cli.InputLine;
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

    @Override
    public void change_id(InputLine inputLine) throws Throwable {
        say("Not supported for Two factor stores");
    }

    @Override
    protected int updateStorePermissions(Identifier newID, Identifier oldID, boolean copy) {
        return 0;
    }
}
