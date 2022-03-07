package org.cilogon.proxy.servlet;

import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.OA2ServiceTransaction;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.storage.clients.OA2Client;
import edu.uiuc.ncsa.security.core.util.Iso8601;
import edu.uiuc.ncsa.security.delegation.storage.ClientApprovalKeys;
import edu.uiuc.ncsa.security.delegation.storage.ClientKeys;
import org.cilogon.d2.twofactor.TwoFactorSerializationKeys;
import org.cilogon.d2.util.DBServiceSerializer;
import org.cilogon.d2.util.IDPKeys;
import org.cilogon.d2.util.UserKeys;
import org.cilogon.oauth2.servlet.impl.Err;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/26/15 at  11:51 AM
 */
public class DBServiceSerializer2 extends DBServiceSerializer {
    public DBServiceSerializer2(UserKeys userKeys, IDPKeys idpKeys, TwoFactorSerializationKeys tfk, ClientKeys cKeys, ClientApprovalKeys caKeys) {
        super(userKeys, idpKeys, tfk, cKeys, caKeys);
    }

    public void writeMessage(PrintWriter w, Err errResponse) throws IOException {
        writeMessage(w, errResponse.code);
        print(w, "error", errResponse.error);
        print(w, "error_description", errResponse.description);
    }

    public void serialize(PrintWriter w, OA2Client oa2Client, int statusCode) throws IOException {
        writeMessage(w, statusCode);
        // Changed in the spec. from the default. See CIL-105

        print(w, "client_id", oa2Client.getIdentifierString());
        print(w, "client_name", oa2Client.getName());
        print(w, "client_limited_proxies", oa2Client.isProxyLimited());
        print(w, "client_email", oa2Client.getEmail());
        print(w, "client_home_uri", oa2Client.getHomeUri());
        print(w, "client_creation_timestamp", Iso8601.date2String(oa2Client.getCreationTS()));
        print(w, "client_refresh_lifetime", (oa2Client.getRtLifetime() == Long.MIN_VALUE) ? "none" : oa2Client.getRtLifetime());
        Collection<String> callbackUris = oa2Client.getCallbackURIs();
        if (callbackUris == null || callbackUris.isEmpty()) {
            return;
        }

        String cbs = "";
        boolean firstPass = true;
        Iterator iterator = callbackUris.iterator();
        while (iterator.hasNext()) {
            cbs = cbs + (firstPass ? "" : ",") + iterator.next();
            if (firstPass) {
                firstPass = false;
            }
        }
        print(w, "client_callback_uris", cbs);

    }

/*    @Override
    protected void doUserSerialization(PrintWriter w, User user) throws IOException {
        super.doUserSerialization(w, user);
        UserKeys uk =  userKeys;
    }*/

    public void serialize(PrintWriter w, OA2ServiceTransaction oa2ServiceTransaction, int status) throws IOException {
        writeMessage(w, status);
    }

    public void serialize(PrintWriter w, OA2ServiceTransaction oa2ServiceTransaction, Err errResponse) throws IOException {
        writeMessage(w, errResponse);
    }
}
