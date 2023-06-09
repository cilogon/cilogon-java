package org.cilogon.oauth2.servlet.util;

import edu.uiuc.ncsa.oa4mp.delegation.common.storage.clients.Client;
import edu.uiuc.ncsa.oa4mp.delegation.common.storage.clients.ClientApprovalKeys;
import edu.uiuc.ncsa.oa4mp.delegation.common.storage.clients.ClientKeys;
import edu.uiuc.ncsa.oa4mp.delegation.server.ServiceTransaction;
import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.core.exceptions.NFWException;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.core.util.Iso8601;
import edu.uiuc.ncsa.security.servlet.ServletDebugUtil;
import edu.uiuc.ncsa.security.storage.XMLMap;
import org.cilogon.oauth2.servlet.StatusCodes;
import org.cilogon.oauth2.servlet.servlet.AbstractDBService;
import org.cilogon.oauth2.servlet.storage.idp.IDPKeys;
import org.cilogon.oauth2.servlet.storage.idp.IdentityProvider;
import org.cilogon.oauth2.servlet.storage.twofactor.TwoFactorInfo;
import org.cilogon.oauth2.servlet.storage.twofactor.TwoFactorSerializationKeys;
import org.cilogon.oauth2.servlet.storage.user.*;

import java.io.*;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.*;

import static edu.uiuc.ncsa.myproxy.oa4mp.server.ServiceConstantKeys.TOKEN_KEY;
import static edu.uiuc.ncsa.myproxy.oa4mp.server.servlet.MyProxyDelegationServlet.getServiceEnvironment;
import static org.cilogon.oauth2.servlet.servlet.AbstractDBService.STATUS_KEY;
import static org.cilogon.oauth2.servlet.servlet.AbstractDBService.distinguishedNameField;


/**
 * A class that serializes to a print writer or deserializes to streams.
 * <p>Created by Jeff Gaynor<br>
 * on Nov 19, 2010 at  11:03:15 AM
 */
public class DBServiceSerializer {
    public DBServiceSerializer(UserKeys userKeys,
                               IDPKeys idpKeys,
                               TwoFactorSerializationKeys tfk,
                               ClientKeys cKeys,
                               ClientApprovalKeys caKeys) {
        this.userKeys = userKeys;
        this.idpKeys = idpKeys;
        this.tfKeys = tfk;
        this.clientKeys = cKeys;
        this.clientApprovalKeys = caKeys;
    }

    public final static String CILOGON_SUCCESS_URI = "cilogon_success";
    public final static String CILOGON_FAILURE_URI = "cilogon_failure";
    public final static String CILOGON_PORTAL_NAME = "cilogon_portal_name";
    public final static String CILOGON_CALLBACK_URI = "cilogon_callback";
    public static final String UTF8_ENCODING = "UTF-8"; // character encoding

    protected UserKeys userKeys;
    protected IDPKeys idpKeys;
    protected TwoFactorSerializationKeys tfKeys;
    protected ClientKeys clientKeys;
    protected ClientApprovalKeys clientApprovalKeys;

    public void writeMessage(PrintWriter w, String message) throws IOException {
        print(w, STATUS_KEY, message);
    }


    public void writeMessage(PrintWriter w, int statusCode) throws IOException {
        print(w, STATUS_KEY, Integer.toString(statusCode));
    }

    public void serialize(PrintWriter w, Map<String, Object> map) throws IOException {
        writeMessage(w, map.get(STATUS_KEY).toString());
        for (String k : map.keySet()) {
            if (!k.equals(STATUS_KEY)) {
                print(w, k, map.get(k).toString());
            }
        }
    }

    public void serialize(PrintWriter w, TwoFactorInfo tfi, int statusCode) throws IOException {
        writeMessage(w, statusCode);
        print(w, tfKeys.identifier(), tfi.getIdentifier());
        print(w, tfKeys.info(), tfi.getInfo());
    }

    public void serialize(PrintWriter w, User user, TwoFactorInfo tfi, int statusCode) throws IOException {
        writeMessage(w, statusCode);
        // do nothing if the tfi doesn't exist or is empty.

        if (!(tfi == null || tfi.getInfo() == null || tfi.getInfo().length() == 0)) {
            print(w, tfKeys.info(), tfi.getInfo());
        }
        doUserSerialization(w, user);


    }

    /**
     * Override this method if you have to extend the user. This is where the user is written
     *
     * @param w
     * @param user
     * @throws IOException
     */
    protected void doUserSerialization(PrintWriter w, User user) throws IOException {
        ServletDebugUtil.trace(this, "in doUserSer: user=" + user);
        if (user.hasRemoteUser()) {
            print(w, userKeys.remoteUser(), user.getRemoteUser());
        }
        if (user.hasEPPN()) {
            print(w, userKeys.eppn(), user.getePPN());
        }
        if (user.hasEPTID()) {
            print(w, userKeys.eptid(), user.getePTID());
        }
        if (user.hasOpenID()) {
            print(w, userKeys.openID(), user.getOpenID());
        }
        if (user.hasOpenIDConnect()) {
            print(w, userKeys.oidc(), user.getOpenIDConnect());
        }
        if (user.hasPairwiseID()) {
            print(w, userKeys.pairwiseId(), user.getPairwiseID());
        }
        if (user.hasSubjectID()) {
            print(w, userKeys.subjectId(), user.getSubjectID());
        }
        onlyPrintIfNotTrivial(w, userKeys.idp(), user.getIdP());
        onlyPrintIfNotTrivial(w, userKeys.idpDisplayName(), user.getIDPName());
        onlyPrintIfNotTrivial(w, userKeys.firstName(), user.getFirstName());
        onlyPrintIfNotTrivial(w, userKeys.lastName(), user.getLastName());
        onlyPrintIfNotTrivial(w, userKeys.userID(), user.getIdentifierString());
        onlyPrintIfNotTrivial(w, userKeys.email(), user.getEmail());
        onlyPrintIfNotTrivial(w, userKeys.serialString(), user.getSerialString());
        onlyPrintIfNotTrivial(w, userKeys.affiliation(), user.getAffiliation());
        onlyPrintIfNotTrivial(w, userKeys.displayName(), user.getDisplayName());
        onlyPrintIfNotTrivial(w, userKeys.organizationalUnit(), user.getOrganizationalUnit());

        try {
            if (user.canGetCert()) {
                print(w, distinguishedNameField, user.getDN(null, true));
            }
        } catch (Exception x) {
            ServletDebugUtil.trace(this, "No DN can be computed for user with id \"" + user.getIdentifierString() + "\"");
            // rock on. If this is a completely new user, this cannot be computed, so return nothing.
        }

        if (user.getAttr_json() != null && !user.getAttr_json().isEmpty()) {
            print(w, userKeys.attr_json(), user.getAttr_json());
        }
        onlyPrintIfNotTrivial(w, userKeys.creationTS(), Iso8601.date2String(user.getCreationTS()));
        if (user.getLastAccessed() != null) {
            onlyPrintIfNotTrivial(w, userKeys.lastAccessed(), Iso8601.date2String(user.getLastAccessed()));
        }
        if (user.getLastModifiedTS() != null) {
            onlyPrintIfNotTrivial(w, userKeys.lastModifiedTS(), Iso8601.date2String(user.getLastModifiedTS()));
        }

    }

    protected void onlyPrintIfNotTrivial(PrintWriter w, String key, String value) throws IOException {
        if (value != null && !value.isEmpty()) {
            print(w, key, value);
        }
    }

    public void serialize(PrintWriter w, User user, int statusCode) throws IOException {
        writeMessage(w, statusCode);
        doUserSerialization(w, user);
    }

    public void serialize(PrintWriter w, Collection<IdentityProvider> idps) throws IOException {
        writeMessage(w, StatusCodes.STATUS_OK);
        for (IdentityProvider idp : idps) {
            print(w, idpKeys.identifier(), idp.getIdentifier());
        }
    }

    public void serialize(PrintWriter w, Identifier userid) throws IOException {
        writeMessage(w, StatusCodes.STATUS_OK);
        print(w, userKeys.identifier(), userid);
    }

    public void serialize(PrintWriter w, ServiceTransaction t) throws IOException {

        writeMessage(w, StatusCodes.STATUS_OK);
        Client client = t.getClient();

        print(w, CILOGON_CALLBACK_URI, t.getCallback());
        print(w, CILOGON_SUCCESS_URI, client.getHomeUri());
        print(w, CILOGON_FAILURE_URI, client.getErrorUri());
        print(w, CILOGON_PORTAL_NAME, client.getName());
        print(w, getServiceEnvironment().getConstants().get(TOKEN_KEY), t.getAuthorizationGrant().getToken());
    }

    /**
     * This takes the serialized payload and pulls it into a simple map. This is mostly
     * used to deserialize things that might not be available to a client, such as a
     * server transaction.
     * <h3>Caveats</h3>
     * <UL>
     * <LI>The status is always stored as a Long</LI>
     * <LI>List values are stored as List&lt;String&gt;'s</LI>
     * <LI>This never throws an exception if there is an issue. You must decide on the course
     * of action from the returned status code.</LI>
     * </UL>
     *
     * @param is
     * @return
     * @throws java.io.IOException
     */
    public XMLMap deserializeToMap(InputStream is) throws IOException {
        XMLMap buffer = new XMLMap();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String linein = br.readLine();
        List<IdentityProvider> idps = null; // just in case it's needed
        if (linein == null || linein.length() == 0) {
            throw new NFWException("Error: service returned a trivial string. This means the service is not responding to requests correctly.");
        }

        try {
            while (linein != null) {
                String[] headAndTail = parseLine(linein);
                if (headAndTail[0].equals(idpKeys.identifier())) {
                    if (idps == null) {
                        idps = new LinkedList<IdentityProvider>();
                        buffer.put(headAndTail[0], idps);
                    }
                    idps.add(new IdentityProvider(BasicIdentifier.newID(headAndTail[1])));
                } else if (headAndTail[0].equals(AbstractDBService.STATUS_KEY)) {
                    buffer.put(headAndTail[0], Long.parseLong(headAndTail[1]));
                } else {
                    buffer.put(headAndTail[0], headAndTail[1]);
                }
                linein = br.readLine();
            }
        } finally {
            br.close();
        }
        return buffer;
    }

    /**
     * Checks that the serialized content of the input stream has an ok as its status. This
     * <b>ignores</b> the rest of the stream and discards it! Only use when you are sure there
     * is nothing else to parse, but want to check that an operation worked. If there are other
     * status codes, this will throw an exception corresponding to the error.
     *
     * @param is
     * @return
     * @throws Exception
     */
    public boolean reponseOk(InputStream is) throws IOException {
        return readResponseOnly(is) % 2 == 0;
    }

    public int readResponseOnly(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String linein = br.readLine();
        String[] x = splitLine(linein);
        br.close();
        try {
            return Integer.parseInt(x[1]);
        } catch (NumberFormatException nfx) {
            throw new GeneralException("Error: unparseable response: " + linein, nfx);
        }
    }

    public Identifier deserializeUserID(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String linein = br.readLine();
        try {
            while (linein != null) {
                String[] headAndTail = parseLine(linein);
                if (headAndTail[0].equals(userKeys.identifier())) return BasicIdentifier.newID(headAndTail[1]);
                linein = br.readLine();
            }
        } finally {
            br.close(); // or the HTTP Client hangs!
        }
        return null;
    }

    /**
     * This takes a user as the argument since the user requires a reference to the namespace
     * resolution machinery of the server environment. It deserializes
     * the stream into it, overwriting any of its content.
     *
     * @param is
     * @param user
     * @return
     * @throws java.io.IOException
     * @throws java.text.ParseException
     */
    public User deserializeUser(InputStream is, User user) throws IOException, ParseException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String linein = br.readLine();
        int lineCount = 0;
        try {
            while (linein != null) {
                String[] headAndTail = parseLine(linein);
                setUserField(user, headAndTail[0], headAndTail[1]);
                linein = br.readLine();
                lineCount++;
            }
            if (lineCount == 1) return null; // there was no user.
            return user;
        } finally {
            br.close(); // or the HTTP Client hangs!
        }
    }


    protected void setUserField(User user, String head, String tail) throws ParseException, UnsupportedEncodingException {
        if (head.equals(userKeys.creationTS())) user.setCreationTS(Iso8601.string2Date(tail).getTime());
        if (head.equals(userKeys.email())) user.setEmail(tail);
        if (head.equals(userKeys.idp())) user.setIdP(tail);
        if (head.equals(userKeys.idpDisplayName())) user.setIDPName(tail);
        if (head.equals(userKeys.firstName())) user.setFirstName(tail);
        if (head.equals(userKeys.lastName())) user.setLastName(tail);
        if (head.equals(userKeys.remoteUser())) {
            if (tail != null) {
                user.setRemoteUser(new RemoteUserName(tail));
            }
        }
        if (head.equals(userKeys.eppn())) {
            if (tail != null) {
                user.setePPN(new EduPersonPrincipleName(tail));
            }
        }
        if (head.equals(userKeys.eptid())) {
            if (tail != null) {
                user.setePTID(new EduPersonTargetedID(tail));
            }
        }
        if (head.equals(userKeys.openID())) {
            if (tail != null) {
                user.setOpenID(new OpenID(tail));
            }
        }

        if (head.equals(userKeys.oidc())) {
            if (tail != null) {
                user.setOpenIDConnect(new OpenIDConnect(tail));
            }
        }

        if (head.equals(userKeys.pairwiseId())) {
            if (tail != null) {
                user.setPairwiseId(new PairwiseID(tail));
            }
        }
        if (head.equals(userKeys.subjectId())) {
            if (tail != null) {
                user.setSubjectId(new SubjectID(tail));
            }
        }

        if (head.equals(userKeys.serialString())) user.setSerialString(tail);
        if (head.equals(userKeys.identifier())) user.setIdentifier(BasicIdentifier.newID(tail));
        if (head.equals(distinguishedNameField)) {
            // check that is returns the same thing or something got messed up in the serialization
            if (!(user.getDN(null, true).toString().equals(tail)))
                throw new CILogonException("Error: the DN's don't match. Returned=\"" + tail + "\", computed=\"" + user.getDN(null, true) + "\".");
        }
    }

    public List<IdentityProvider> deserializeIdps(InputStream is) throws IOException {
        List<IdentityProvider> idps = new ArrayList<IdentityProvider>();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String linein = br.readLine();
        try {
            while (linein != null) {
                String[] headAndTail = parseLine(linein);
                if (headAndTail[0].equals(idpKeys.identifier()))
                    idps.add(new IdentityProvider(BasicIdentifier.newID(headAndTail[1])));
                linein = br.readLine();
            }
        } finally {
            br.close();
        }
        return idps;
    }

    protected String[] splitLine(String linein) throws UnsupportedEncodingException {
        int pos = linein.indexOf("=");
        String head = linein.substring(0, pos);
        String tail = decode(linein.substring(pos + 1));
        return new String[]{head, tail};

    }

    protected String[] parseLine(String linein) throws UnsupportedEncodingException {
        String[] x = splitLine(linein);
        checkForStatus(x[0], x[1]);
        return x;
    }

    /**
     * Checks the status line in the serialized object for error codes and throws a corresponding
     * exception. If the status is ok, the call succeeds.
     *
     * @param head
     * @param tail
     */
    protected void checkForStatus(String head, String tail) {
        if (head.equals(STATUS_KEY)) {
            // Even return  codes are ok and informational.
            if (Integer.parseInt(tail) % 2 == 0) return;
            ServletDebugUtil.trace(this, "Got unrecognized response of head=\"" + head + "\" tail=\"" + tail + "\"");
            throw new DBServiceException(tail);
        }
    }


    protected String encode(String x) throws UnsupportedEncodingException {
        return URLEncoder.encode(x, UTF8_ENCODING);
    }

    protected String encode(URI x) throws UnsupportedEncodingException {
        return encode(x.toString());
    }

    public String decode(String x) throws UnsupportedEncodingException {
        return URLDecoder.decode(x, UTF8_ENCODING);
    }

    protected void print(PrintWriter w, String key, Identifier identifier) throws IOException {
        print(w, key, identifier == null ? "" : identifier.toString());
    }

    protected void print(PrintWriter w, String key, URI uri) throws IOException {
        print(w, key, uri == null ? "" : uri.toString());
    }

    protected void print(PrintWriter w, String key, Date date) throws IOException {
        print(w, key, Iso8601.date2String(date));
    }

    public void print(PrintWriter w, String key, String value) throws IOException {
        w.println(key + "=" + (value == null ? "" : encode(value)));
    }

    public void print(PrintWriter w, String key, Object value) throws IOException {
        w.println(key + "=" + (value == null ? "" : encode(value.toString())));
    }
}
