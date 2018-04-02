package org.cilogon.d2.servlet;

import edu.uiuc.ncsa.myproxy.MyProxyServiceFacade;
import edu.uiuc.ncsa.myproxy.NoUsableMyProxyServerFoundException;
import edu.uiuc.ncsa.myproxy.oa4mp.server.ServiceConstantKeys;
import edu.uiuc.ncsa.myproxy.oa4mp.server.servlet.AbstractAuthorizationServlet;
import edu.uiuc.ncsa.myproxy.oa4mp.server.servlet.MyProxyDelegationServlet;
import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.core.exceptions.InvalidTokenException;
import edu.uiuc.ncsa.security.core.exceptions.NotImplementedException;
import edu.uiuc.ncsa.security.delegation.server.ServiceTransaction;
import edu.uiuc.ncsa.security.delegation.server.request.IssuerResponse;
import edu.uiuc.ncsa.security.delegation.token.AuthorizationGrant;
import edu.uiuc.ncsa.security.delegation.token.MyX509Certificates;
import edu.uiuc.ncsa.security.delegation.token.Token;
import edu.uiuc.ncsa.security.delegation.token.Verifier;
import edu.uiuc.ncsa.security.util.pkcs.CertUtil;
import edu.uiuc.ncsa.security.util.pkcs.PEMFormatUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpStatus;
import org.cilogon.d2.storage.User;
import org.cilogon.d2.storage.UserStore;
import org.cilogon.d2.util.CILogonException;
import org.cilogon.d2.util.CILogonSE;
import org.cilogon.d2.util.CILogonServiceTransaction;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Collection;

import static edu.uiuc.ncsa.security.core.util.BasicIdentifier.newID;
import static edu.uiuc.ncsa.security.core.util.DateUtils.checkTimestamp;
import static edu.uiuc.ncsa.security.util.pkcs.CertUtil.toPEM;

/**
 * This does not extend OA4MP {@link AbstractAuthorizationServlet}
 * since it does not do authorization. This servlet is called by the authorization component after
 * the user has been authenticated. In other words, this is called <b>after</b> authentication has taken
 * place successfully. As per spec, successful authentication is recorded by setting the authorization grant to
 * being valid.
 * <p>Created by Jeff Gaynor<br>
 * on 3/1/12 at  12:10 PM
 */
public abstract class AbstractAuthorizedServlet extends MyProxyDelegationServlet {
    @Override
    public ServiceTransaction verifyAndGet(IssuerResponse iResponse) throws IOException {
        return null;
    }

    public abstract String createRedirect(URI baseUri, Identifier identifier, Token verifier);

    @Override
    protected void doIt(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Throwable {
        try {
            info("6.a. Delegation authorized. Starting checks...");

            CILogonServiceTransaction trans = getAndCheckTransaction(httpServletRequest);
            MyX509Certificates myCert = null;
            if (trans.getClient().isProxyLimited()) {
                throw new NotImplementedException("Limited proxies are not implemented in CILogon.");
            } else {
                myCert = doMyProxy(trans);
            }
            trans.setProtectedAsset(myCert);
            trans.setVerifier(MyProxyDelegationServlet.getServiceEnvironment().getTokenForge().getVerifier());
            trans.setAccessTokenValid(false);
            getServiceEnvironment().getTransactionStore().save(trans);

            String xx = " transaction=" + trans.getIdentifierString() + " and client=" + trans.getClient().getIdentifierString();

            debug("4.a. verifier = " + trans.getVerifier() + ", " + xx);
            String cb = createRedirect(trans.getCallback(), trans.getIdentifier(), trans.getVerifier());
            info("4.a. writing redirect uri for  " + cb + ", " + xx);

            Writer w = httpServletResponse.getWriter();
            String returnedString = STATUS_PARAMETER + "=" + CILOGON_STATUS_OK + "\n";
            httpServletResponse.setStatus(HttpStatus.SC_OK);

            String x = toPEM(myCert.getX509Certificates());
            String cilogonCert = CERT_PARAMETER + "=" + PEMFormatUtil.bytesToChunkedString(x.getBytes());
            cilogonCert = cilogonCert.replaceAll("\r\n", ""); // Commons puts in \r\n rather than standard Java \n!!
            returnedString = returnedString + cilogonCert;
            returnedString = returnedString + "\n" + CILOGON_RESPONSE_URL + "=" + cb;
            w.write(returnedString);
            w.close();
            info("4.b Finished authZ servlet " + xx);
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    public static final String CILOGON_INFO = "cilogon_info";
    String UTF8_ENCODING = "UTF-8"; // character encoding

    public final static String UID_PARAMETER = "cilogon_uid";
    public final static String CERT_REQUEST_PARAMETER = "cilogon_cert_request";
    public final static String CERT_REQUEST_ID = "cilogon_cert_req_id";

    public final static String LOA_PARAMETER = "cilogon_loa";
    public final static String LIFETIME_PARAMETER = "cilogon_lifetime";
    public final static String STATUS_PARAMETER = "cilogon_status";
    public final static String CILOGON_STATUS_OK = "ok";
    public final static String CILOGON_STATUS_ERROR = "error";
    public final static String CERT_PARAMETER = "cilogon_cert";
    public final static String CILOGON_RESPONSE_URL = "cilogon_response_url";

    /**
     * This checks the request parameters and gets the corresponding transaction. Since this does not use
     * the OAuth libraries, we have to pull it apart ourselves.
     *
     * @param httpServletRequest
     * @return
     * @throws Throwable
     */
    protected CILogonServiceTransaction getAndCheckTransaction(HttpServletRequest httpServletRequest) throws Throwable {
        String temp = httpServletRequest.getParameter(UID_PARAMETER);
        if (temp == null) {
            throw new ServletException("Error: missing parameter for the user id");
        }

        URI uid = URI.create(temp);
        if (!getUserStore().containsKey(uid)) {
            throw new ServletException("Error: unknown user id \"" + uid + "\"");
        }
        String loa = httpServletRequest.getParameter(LOA_PARAMETER);
        if (loa == null) {
            throw new ServletException("Error: missing parameter for the level of assurance");
        }
        // This comes to us in hours. The stored value is always in ms!
        temp = httpServletRequest.getParameter(LIFETIME_PARAMETER);
        if (temp == null) {
            throw new ServletException("Error: missing parameter for the lifetime");
        }

        long lifetime = Long.parseLong(temp) * 3600 * 1000;
        // before we set the lifetime, we have to make sure it is correct
        if (Integer.MAX_VALUE <= lifetime / 1000) {
            // unlikely, but you never know... Throws an exception if it won't fit in an integer when converted to seconds
            throw new ServletException("Error: the max lifetime (" + (lifetime / 1000) + " seconds) is too large");
        }


        // now to get the transaction
        temp = httpServletRequest.getParameter(getServiceEnvironment().getConstants().get(ServiceConstantKeys.TOKEN_KEY));
        if (temp == null) {
            throw new ServletException("Error: missing parameter for the token");
        }


        AuthorizationGrant authorizationGrant = getServiceEnvironment().getTokenForge().getAuthorizationGrant(temp);
        CILogonServiceTransaction trans;

        try {
            trans = (CILogonServiceTransaction) getServiceEnvironment().getTransactionStore().get(authorizationGrant);
        } catch (CILogonException e) {
            throw new IllegalStateException("Error getting transaction", e);
        }
        if (trans == null) {
            throw new InvalidTokenException("No transaction for token=\"" + authorizationGrant + "\"");
        }
        checkTimestamp(authorizationGrant.getToken());

        if (trans.isComplete()) {
            throw new GeneralException("Error: The cert request is complete.");
        }
        // We don't have the username that the user signed in with, since we don't have access to their
        // login. Best was can do is store the user id we have.
        trans.setUsername(uid.toString()); // here's where we can set it
        trans.setLifetime(lifetime);
        info("4.a. Setting LOA to " + loa);
        trans.setLoa(loa);
        debug("getting cilogon_info");
        // cilogon_info actually has the (overloaded) username for MyProxy. Other information is often included.
        String cilogonInfo = httpServletRequest.getParameter(CILOGON_INFO);
        if (cilogonInfo != null && cilogonInfo.length() != 0) {
            debug("cilogon_info=\"" + cilogonInfo + "\"");
            // this should be url encoded,
            trans.setMyproxyUsername(URLDecoder.decode(cilogonInfo, UTF8_ENCODING));
        } else {
            debug("NO CILOGON_INFO!");
        }
        String logEntry = "4.a. Got request for " +
                "\n\tuserid=" + uid +
                "\n\tcilogon_info= " + cilogonInfo +
                "\n\tlifetime=" + lifetime +
                "\n\tcb=" + trans.getCallback() +
                "\n\ttc=" + getTCToken(trans.getIdentifierString());
        info(logEntry);

        trans.setAuthGrantValid(true); // as per spec, temp cred can now be set to valid.
        Verifier verifier = getServiceEnvironment().getTokenForge().getVerifier();
        trans.setVerifier(verifier);
        getServiceEnvironment().getTransactionStore().save(trans);
        return trans;
    }

    /* The next set of infoID calls are all for spitting out formatted info statements to the logs */


    protected String infoID(URI callback, String idToken) {
        return infoID(callback.toString(), idToken);
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

    protected String infoID(String callback, String tempCred) {
        return "(cb=" + callback + ", tc=" + getTCToken(tempCred) + ")";
    }

    CILogonSE getSE() throws IOException {
        return (CILogonSE) getServiceEnvironment();
    }

    protected UserStore getUserStore() throws IOException {
        return getSE().getUserStore();
    }


    protected MyX509Certificates doMyProxy(CILogonServiceTransaction t) throws Throwable {
        debug(getClass().getSimpleName() + ".doMyProxy: starting");
        User user = getUserStore().get(newID(t.getUsername()));
        String xx = " transaction=" + t.getIdentifierString() + " and client=" + t.getClient().getIdentifierString();

        String dn = user.getDN(t, true);
        debug("userDN=" + dn);
        debug("myproxyUsername=" + t.getMyproxyUsername());
        if (t.getMyproxyUsername() != null && 0 < t.getMyproxyUsername().length()) {
            // append extra information. Spec says *one* blank between DN and additional info
            dn = dn.trim() + " " + t.getMyproxyUsername();
            debug("Got additional user info = \"" + t.getMyproxyUsername() + "\", user name=\"" + dn + "\"");
        } else {
            debug("NO additional user info = ");
        }
        debug("extended DN=" + dn);
        Collection<X509Certificate> certs = null;
        byte[] derCertRequest = Base64.decodeBase64(t.getCertReqString());

        info("3.b. MP facade #=" + MyProxyDelegationServlet.getServiceEnvironment().getMyProxyServices().size());
        debug("3.b. MP facade #=" + MyProxyDelegationServlet.getServiceEnvironment().getMyProxyServices().size());
        for (MyProxyServiceFacade msp : MyProxyDelegationServlet.getServiceEnvironment().getMyProxyServices()) {
            info("3.b. myproxy is " + msp.getFacadeConfiguration().getHostname() + ":" + msp.getFacadeConfiguration().getPort());
            try {
                debug("Getting cert with username = \"" + dn + "\"");
                certs = msp.getCerts(dn, derCertRequest, t.getLifetime(), t.getLoa());
                if (certs != null && !certs.isEmpty()) {
                    // CIL-194: this continues to get certs. It should break on the first found.
                   debug("EPPN = " + CertUtil.getEPPN(certs.iterator().next())); // REMOVE THIS
                    break;
                }
            } catch (GeneralSecurityException gsx) {
                error("failed to get cert for token " + xx + ", message = " + gsx.getMessage());
                throw gsx;
            } catch (Throwable tt) {
                info("3.b. returned exception is " + tt.getClass().getName());
                info("**REMOVE STACK TRACE**");
                // FIX ME Remove before rolling in production
          tt.printStackTrace();
            }
        }
        if (certs == null || certs.isEmpty()) {
            info("Error: No usable MyProxy service found." + xx);
            throw new NoUsableMyProxyServerFoundException("Error: No usable MyProxy service found.");
        }

        info("3.c. Got cert from MyProxy & storing it, " + xx);

        MyX509Certificates myCert = new MyX509Certificates(certs);
        t.setProtectedAsset(myCert);
        return myCert;
    }
}
