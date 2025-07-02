package org.cilogon.oauth2.admin;

import edu.uiuc.ncsa.security.core.Identifiable;
import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.Store;
import edu.uiuc.ncsa.security.util.cli.CLIDriver;
import org.cilogon.oauth2.servlet.storage.idp.IdentityProviderStore;
import org.oa4mp.server.admin.oauth2.base.OA4MPStoreCommands;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 11/4/13 at  3:54 PM
 */
public class IDPCommands extends OA4MPStoreCommands {

    public IDPCommands(CLIDriver driver, String defaultIndent, Store store) throws Throwable {
        super(driver, defaultIndent, store);
    }

    public IDPCommands(CLIDriver driver, Store store)throws Throwable {
        super(driver, store);
    }

    protected IdentityProviderStore getIDPStore() {
        return (IdentityProviderStore) getStore();
    }

    @Override
    protected String format(Identifiable identifiable) {
        return identifiable.getIdentifierString();
    }

    @Override
    public String getName() {
        return "idp";
    }

    @Override
    protected int updateStorePermissions(Identifier newID, Identifier oldID, boolean copy) {
        return 0; // no permissions for IDPs
    }

    @Override
    protected void initHelp() throws Throwable {
        super.initHelp();
        getHelpUtil().load("/help/idp_help.xml");
    }
}
