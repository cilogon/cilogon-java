package org.cilogon.oauth1.servlet.impl;

import edu.uiuc.ncsa.security.delegation.storage.ClientApprovalKeys;
import edu.uiuc.ncsa.security.delegation.storage.ClientKeys;
import org.cilogon.d2.servlet.AbstractDBService;
import org.cilogon.d2.twofactor.TwoFactorSerializationKeys;
import org.cilogon.d2.util.DBServiceSerializer;
import org.cilogon.d2.util.IDPKeys;
import org.cilogon.d2.util.UserKeys;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/22/15 at  12:06 PM
 */
public class DBService extends AbstractDBService {
    @Override
       public void init(ServletConfig config) throws ServletException {
           super.init(config);

           userKeys = new UserKeys();
           idpKeys = new IDPKeys();
           tfKeys = new TwoFactorSerializationKeys();
           clientKeys = new ClientKeys();
           clientApprovalKeys = new ClientApprovalKeys();
           serializer = new DBServiceSerializer(userKeys, idpKeys, tfKeys, clientKeys, clientApprovalKeys);

       }
}
