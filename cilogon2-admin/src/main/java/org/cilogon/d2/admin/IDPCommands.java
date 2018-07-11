package org.cilogon.d2.admin;

import edu.uiuc.ncsa.myproxy.oa4mp.server.StoreCommands2;
import edu.uiuc.ncsa.security.core.Identifiable;
import edu.uiuc.ncsa.security.core.Store;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import org.cilogon.d2.storage.IdentityProvider;
import org.cilogon.d2.storage.IdentityProviderStore;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 11/4/13 at  3:54 PM
 */
public class IDPCommands extends StoreCommands2 {
    @Override
    public void extraUpdates(Identifiable identifiable) {
    }

    public IDPCommands(MyLoggingFacade logger, String defaultIndent, Store store) {
        super(logger, defaultIndent, store);
    }

    public IDPCommands(MyLoggingFacade logger, Store store) {
        super(logger, store);
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
        return "  idp";
    }

    @Override
    public boolean update(Identifiable identifiable) {
        info("Updating IDP =" + identifiable.getIdentifierString());
        getIDPStore().save(new IdentityProvider(identifiable.getIdentifier()));
        sayi("save changes [y/n]?");
        return isOk(readline());
    }

    @Override
    protected void longFormat(Identifiable identifiable) {
        sayi(identifiable.getIdentifierString());
    }



}
