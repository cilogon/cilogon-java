package org.cilogon.proxy.servlet;

import org.apache.http.HttpStatus;
import org.cilogon.oauth2.servlet.loader.CILogonOA2ServiceEnvironment;
import org.cilogon.oauth2.servlet.storage.transaction.CILOA2ServiceTransaction;
import org.cilogon.oauth2.servlet.storage.user.User;
import org.oa4mp.delegation.server.OA2Errors;
import org.oa4mp.delegation.server.OA2GeneralError;
import org.oa4mp.delegation.server.ServiceTransaction;
import org.oa4mp.server.loader.oauth2.storage.transactions.OA2ServiceTransaction;
import org.oa4mp.server.proxy.OA2CertServlet;

import java.security.GeneralSecurityException;

import static edu.uiuc.ncsa.security.core.util.BasicIdentifier.newID;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 9/14/15 at  4:16 PM
 */
public class CILOA2CertServlet extends OA2CertServlet {
    String noDnMessage = "The user is missing information required for a cert.";

    @Override
    protected void checkMPConnection(OA2ServiceTransaction st) throws GeneralSecurityException {
        if (!hasMPConnection(st)) {
            CILOA2ServiceTransaction t = (CILOA2ServiceTransaction) st;
            User user = ((CILogonOA2ServiceEnvironment) getServiceEnvironment()).getUserStore().get(newID(t.getUsername()));
            if(!user.canGetCert()){
                throw new OA2GeneralError(OA2Errors.ACCESS_DENIED,
                        noDnMessage,
                        HttpStatus.SC_FORBIDDEN,
                        st.getRequestState());
            }

            String dn = user.getDN(t, true);
            if (t.getMyproxyUsername() != null && 0 < t.getMyproxyUsername().length()) {
                // append extra information. Spec says *one* blank between DN and additional info
                dn = dn.trim() + " " + t.getMyproxyUsername();
                debug("Got additional user info = \"" + t.getMyproxyUsername() + "\", user name=\"" + dn + "\"");
            } else {
                debug("NO additional user info = ");
            }
            debug("extended DN=" + dn);
            createMPConnection(st.getIdentifier(), dn, "", st.getLifetime(), ((CILOA2ServiceTransaction) st).getLoa());
        }

    }

    @Override
    protected void doCertRequest(ServiceTransaction trans, String statusString) throws Throwable {
        CILOA2ServiceTransaction t = (CILOA2ServiceTransaction) trans;
        
        User user = ((CILogonOA2ServiceEnvironment) getServiceEnvironment()).getUserStore().get(newID(t.getUsername()));
        if(!user.canGetCert()){
            throw new OA2GeneralError(OA2Errors.ACCESS_DENIED,
                    noDnMessage ,
                    HttpStatus.SC_FORBIDDEN,
                    ((CILOA2ServiceTransaction) trans).getRequestState());
        }
        super.doCertRequest(trans, statusString);
    }
}
