package org.cilogon.oauth1.servlet.impl;

import edu.uiuc.ncsa.myproxy.oa4mp.server.ServiceConstantKeys;
import edu.uiuc.ncsa.myproxy.oa4mp.server.servlet.AuthorizedServlet;
import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.core.exceptions.NotImplementedException;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.delegation.server.ServiceTransaction;
import edu.uiuc.ncsa.security.delegation.server.request.IssuerResponse;
import edu.uiuc.ncsa.security.delegation.token.AccessToken;
import edu.uiuc.ncsa.security.delegation.token.MyX509Certificates;
import edu.uiuc.ncsa.security.delegation.token.Verifier;
import edu.uiuc.ncsa.security.util.pkcs.CertUtil;
import edu.uiuc.ncsa.security.util.pkcs.PEMFormatUtil;
import net.oauth.OAuth;
import org.apache.http.HttpStatus;
import org.cilogon.d2.storage.User;
import org.cilogon.d2.storage.UserStore;
import org.cilogon.d2.util.CILogonSE;
import org.cilogon.d2.util.CILogonServiceTransaction;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URLDecoder;

import static edu.uiuc.ncsa.security.core.util.BasicIdentifier.newID;
import static edu.uiuc.ncsa.security.util.pkcs.CertUtil.toPEM;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 7/17/15 at  2:00 PM
 */
public class NewAuthorizedServlet extends AuthorizedServlet {
    @Override
    protected AccessToken getAccessToken(HttpServletRequest request) {
        throw new NotImplementedException("No need for access tokens in this servlet");
    }

    @Override
    public String createCallback(ServiceTransaction trans) {
        return trans.getCallback() + "?" + OAuth.OAUTH_TOKEN + "=" + trans.getIdentifierString() + "&" + OAuth.OAUTH_VERIFIER + "=" + trans.getVerifier().getToken();
    }

    @Override
    public ServiceTransaction verifyAndGet(IssuerResponse iResponse) throws IOException {
        return null;
    }

    public static class CILogonPP extends ProtocolParameters {
        String cilogonInfo;
    }

    @Override
    protected void writeResponse(HttpServletResponse response, ServiceTransaction trans) throws IOException {
        String xx = trans.getClient().getIdentifierString();
        debug("4.a. verifier = " + trans.getVerifier() + ", " + xx);
        String cb = createCallback(trans);
        info("4.a. writing redirect uri for  " + cb + ", " + xx);

        Writer w = response.getWriter();
        String returnedString = STATUS_PARAMETER + "=" + CILOGON_STATUS_OK + "\n";
        response.setStatus(HttpStatus.SC_OK);
        MyX509Certificates myCert = (MyX509Certificates) trans.getProtectedAsset();
        String x = toPEM(myCert.getX509Certificates());
        String cilogonCert = CERT_PARAMETER + "=" + PEMFormatUtil.bytesToChunkedString(x.getBytes());
        cilogonCert = cilogonCert.replaceAll("\r\n", ""); // Commons puts in \r\n rather than standard Java \n!!
        returnedString = returnedString + cilogonCert;
        returnedString = returnedString + "\n" + CILOGON_RESPONSE_URL + "=" + cb;
        w.write(returnedString);
        // No redirect set here since the main CILogon app will do that.
        w.close();
    }


    @Override
    protected ProtocolParameters parseRequest(HttpServletRequest request) throws ServletException {
        CILogonPP p = new CILogonPP();
        p.userId = request.getParameter(UID_PARAMETER);
        if (p.userId == null) {
            throw new ServletException("Error: missing parameter for the user id");
        }
        p.loa = request.getParameter(LOA_PARAMETER);
        if (p.loa == null) {
            throw new ServletException("Error: missing parameter for the level of assurance");
        }
        p.token = request.getParameter(getServiceEnvironment().getConstants().get(ServiceConstantKeys.TOKEN_KEY));
        if (p.token == null) {
            throw new ServletException("Error: missing parameter for the token");
        }

        // parse the lifetime

        String temp = request.getParameter(LIFETIME_PARAMETER);
        if (temp == null) {
            throw new ServletException("Error: missing parameter for the lifetime");
        }

        p.lifetime = Long.parseLong(temp) * 3600 * 1000;
        // before we set the lifetime, we have to make sure it is correct
        if (Integer.MAX_VALUE <= p.lifetime / 1000) {
            // unlikely, but you never know... Throws an exception if it won't fit in an integer when converted to seconds
            throw new ServletException("Error: the max lifetime (" + (p.lifetime / 1000) + " seconds) is too large");
        }

        debug("getting cilogon_info");
        // cilogon_info actually has the (overloaded) username for MyProxy. Other information is often included.
        p.cilogonInfo = request.getParameter(CILOGON_INFO);

        return p;
    }


    @Override
    protected void doRealCertRequest(ServiceTransaction trans, String statusString) throws Throwable {
        super.doCertRequest(trans, statusString);
    }

    public static final String CILOGON_INFO = "cilogon_info";
    public final static String LIFETIME_PARAMETER = "cilogon_lifetime";
    public final static String UID_PARAMETER = "cilogon_uid";
    public final static String LOA_PARAMETER = "cilogon_loa";
    String UTF8_ENCODING = "UTF-8"; // character encoding

    // Old. Keep here for a bit just in case.
    public final static String CERT_REQUEST_ID = "cilogon_cert_req_id";
    public final static String CERT_REQUEST_PARAMETER = "cilogon_cert_request";
    public final static String STATUS_PARAMETER = "cilogon_status";
    public final static String CILOGON_STATUS_OK = "ok";
    public final static String CILOGON_STATUS_ERROR = "error";
    public final static String CERT_PARAMETER = "cilogon_cert";
    public final static String CILOGON_RESPONSE_URL = "cilogon_response_url";

    /**
     * This checks the request parameters and gets the corresponding transaction. Since this does not use
     * the OAuth libraries, we have to pull it apart ourselves.
     *
     * @param p
     * @return
     * @throws Throwable
     */
    protected ServiceTransaction getAndCheckTransaction(ProtocolParameters p) throws Throwable {
        CILogonPP ciLogonPP = (CILogonPP) p;
        CILogonServiceTransaction trans = (CILogonServiceTransaction) super.getAndCheckTransaction(p);
        URI uid = URI.create(ciLogonPP.userId);
        Identifier uid2 = BasicIdentifier.newID(uid);
        if (!getUserStore().containsKey(uid2)) {
            throw new ServletException("Error: unknown user id \"" + uid + "\"");
        }
        if (trans.isComplete()) {
            throw new GeneralException("Error: The cert request is complete.");
        }

        if (trans.getClient().isProxyLimited()) {
            throw new NotImplementedException("Limited proxies are not implemented in CILogon.");
        }

        // We don't have the username that the user signed in with, since we don't have access to their
        // login. Best was can do is store the user id we have.
        trans.setUsername(uid.toString()); // here's where we can set it
        trans.setLifetime(ciLogonPP.lifetime);
        info("4.a. Setting LOA to " + ciLogonPP.loa);
        trans.setLoa(ciLogonPP.loa);
        if (ciLogonPP.cilogonInfo != null && ciLogonPP.cilogonInfo.length() != 0) {
            debug("cilogon_info=\"" + ciLogonPP.cilogonInfo + "\"");
            // this should be url encoded,
            trans.setMyproxyUsername(URLDecoder.decode(ciLogonPP.cilogonInfo, UTF8_ENCODING));
        } else {
            debug("NO CILOGON_INFO!");
        }
        String logEntry = "4.a. Got request for " +
                "\n\tuserid=" + uid +
                "\n\tcilogon_info= " + ciLogonPP.cilogonInfo +
                "\n\tlifetime=" + ciLogonPP.lifetime +
                "\n\tcb=" + trans.getCallback() +
                "\n\ttc=" + getTCToken(trans.getIdentifierString());
        info(logEntry);

        trans.setAuthGrantValid(true); // as per spec, temp cred can now be set to valid.
        Verifier verifier = getServiceEnvironment().getTokenForge().getVerifier();
        trans.setVerifier(verifier);
        getServiceEnvironment().getTransactionStore().save(trans);
        return trans;
    }

    protected String getTCToken(String tempCred) {
        String x = "(none)";
        if (tempCred != null) {
            int lastSlashIndex = tempCred.lastIndexOf("/");
            int nextLastSlashIndex = tempCred.lastIndexOf("/", lastSlashIndex - 1);
            x = tempCred.substring(nextLastSlashIndex + 1, lastSlashIndex);
        }
        return x;
    }

    CILogonSE getSE() throws IOException {
        return (CILogonSE) getServiceEnvironment();
    }

    protected UserStore getUserStore() throws IOException {
        return getSE().getUserStore();
    }


    @Override
    protected void doIt(HttpServletRequest request, HttpServletResponse response) throws Throwable {

        ProtocolParameters p = parseRequest(request);
        CILogonServiceTransaction trans = (CILogonServiceTransaction) getAndCheckTransaction(p);
        trans.setUsername(p.userId); /// this is the internal identifier for this user.
        User user = getUserStore().get(newID(p.userId));

        String dn = user.getDN(trans, true);
        debug("userDN=" + dn);
        debug("myproxyUsername=" + trans.getMyproxyUsername());
        if (trans.getMyproxyUsername() != null && 0 < trans.getMyproxyUsername().length()) {
            // append extra information. Spec says *one* blank between DN and additional info
            dn = dn.trim() + " " + trans.getMyproxyUsername();
            debug("Got additional user info = \"" + trans.getMyproxyUsername() + "\", user name=\"" + dn + "\"");
        } else {
            debug("NO additional user info = ");
        }
        debug("extended DN=" + dn);
        // CILogon presumes a trust relation to the MyProxy server, so no password is needed, but the user DN is.
        getTransactionStore().save(trans); // keep the user name
        createMPConnection(trans.getIdentifier(), dn, null, p.lifetime, p.loa);
        doRealCertRequest(trans, "");
        MyX509Certificates myCerts = (MyX509Certificates) trans.getProtectedAsset();

        debug("EPPN = " + CertUtil.getEPPN(myCerts.getX509Certificates()[0])); // REMOVE THIS

        writeResponse(response, trans);
    }
}
