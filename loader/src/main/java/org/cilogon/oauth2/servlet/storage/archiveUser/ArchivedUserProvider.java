package org.cilogon.oauth2.servlet.storage.archiveUser;

import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.util.IdentifiableProviderImpl;
import edu.uiuc.ncsa.security.core.util.IdentifierProvider;

import java.util.Date;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/5/12 at  11:18 AM
 */
public class ArchivedUserProvider extends IdentifiableProviderImpl<ArchivedUser> {

    public ArchivedUserProvider(IdentifierProvider<Identifier> idProvider) {
        super(idProvider);
    }

    @Override
    public ArchivedUser get(boolean createNewIdentifier) {
        ArchivedUser au = new ArchivedUser(createNewId(createNewIdentifier));
        au.setArchivedDate(new Date());
        return au;
    }

    /**
     * This exposes the identifier creation.
     * @return
     */
    public Identifier newId(){
        return idProvider.get();
    }
}
