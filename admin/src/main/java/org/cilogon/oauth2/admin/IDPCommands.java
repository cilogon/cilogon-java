package org.cilogon.oauth2.admin;

import edu.uiuc.ncsa.myproxy.oauth2.base.StoreCommands2;
import edu.uiuc.ncsa.security.core.Identifiable;
import edu.uiuc.ncsa.security.core.Store;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import org.cilogon.oauth2.servlet.storage.idp.IdentityProviderStore;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 11/4/13 at  3:54 PM
 */
public class IDPCommands extends StoreCommands2 {

    public IDPCommands(MyLoggingFacade logger, String defaultIndent, Store store) throws Throwable {
        super(logger, defaultIndent, store);
    }

    public IDPCommands(MyLoggingFacade logger, Store store)throws Throwable {
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

/*    @Override
    public boolean update(Identifiable identifiable) throws IOException {
        info("Updating IDP =" + identifiable.getIdentifierString());
        getIDPStore().save(new IdentityProvider(identifiable.getIdentifier()));
        return isOk(readline("save changes [y/n]?"));
    }*/

    @Override
    public void bootstrap() throws Throwable {
        super.bootstrap();
        getHelpUtil().load("/help/idp_help.xml");
    }
}
