package org.cilogon.oauth2.servlet.util;

import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.core.exceptions.NFWException;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.core.util.Pool;
import edu.uiuc.ncsa.security.storage.XMLMap;
import edu.uiuc.ncsa.security.util.ssl.MyTrustManager;
import edu.uiuc.ncsa.security.util.ssl.SSLConfiguration;
import edu.uiuc.ncsa.security.util.ssl.VerifyingHTTPClientFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.cilogon.oauth2.servlet.StatusCodes;
import org.cilogon.oauth2.servlet.servlet.AbstractDBService;
import org.cilogon.oauth2.servlet.storage.idp.IDPKeys;
import org.cilogon.oauth2.servlet.storage.idp.IdentityProvider;
import org.cilogon.oauth2.servlet.storage.twofactor.TwoFactorInfo;
import org.cilogon.oauth2.servlet.storage.twofactor.TwoFactorSerializationKeys;
import org.cilogon.oauth2.servlet.storage.user.PersonName;
import org.cilogon.oauth2.servlet.storage.user.User;
import org.cilogon.oauth2.servlet.storage.user.UserKeys;
import org.cilogon.oauth2.servlet.storage.user.UserMultiID;
import org.oa4mp.delegation.common.storage.clients.ClientApprovalKeys;
import org.oa4mp.delegation.common.storage.clients.ClientKeys;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is a client that talks to a AbstractDBService.
 * <p>Created by Jeff Gaynor<br>
 * on 5/21/12 at  1:43 PM
 */
public class DBServiceClient {

    public DBServiceClient(String host, String tokenKey) {
        this.host = host;
        this.tokenKey = tokenKey;
    }

    /**
     * The tokenKey is the key in the store that corresponds to the authorization grant. This may vary from
     * service to service.
     */
    String tokenKey;
    String host;

    public String host(String... x) {
        if (0 < x.length) host = x[0];
        return host;
    }

    VerifyingHTTPClientFactory vcf;

     VerifyingHTTPClientFactory getVCF() throws IOException {
         if (vcf == null) {
             vcf = new VerifyingHTTPClientFactory(null, getSslConfiguration());
         }
         return vcf;
     }

    public SSLConfiguration getSslConfiguration() {
         if(sslConfiguration == null){
              sslConfiguration = new SSLConfiguration();

             sslConfiguration.setTrustRootPath(System.getProperty("store"));
             sslConfiguration.setTrustRootPassword(System.getProperty("password"));
             sslConfiguration.setTrustRootCertDN("CN=localhost");
             sslConfiguration.setTrustRootType("JKS");

         }
        return sslConfiguration;
    }

    public void setSslConfiguration(SSLConfiguration sslConfiguration) {
        this.sslConfiguration = sslConfiguration;
    }

    SSLConfiguration sslConfiguration;

    Pool<HttpClient> clientPool = new Pool<HttpClient>() {
        @Override
        public HttpClient create() {
            try {
                MyTrustManager myTrustManager = new MyTrustManager(null, getSslConfiguration());
                return getVCF().getClient(myTrustManager);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            // Issue is that the server now redirects everything to https and this client does not
            // have the self-signed cert for that. Need to configure this so tests work locally again.
            return HttpClientBuilder.create()
                    .setRedirectStrategy(new LaxRedirectStrategy()).build();
        }


        @Override
        public void destroy(HttpClient httpClient) {
            // stateless so nothing to do really.
        }
    };


    public XMLMap doGet(String action, XMLMap map) {
        return doGet(action, createRequest(map));
    }

    protected XMLMap doGet(String action, String[][] args) {
        String getString = host() + "?" + AbstractDBService.ACTION_PARAMETER + "=" + action;
        if (args != null && args.length != 0) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].length != 0) {
                    try {
                        // We have to encode the string to UTF-8 since we are doing an http GET.
                        // The HTML spec says non-ASCII characters must be escaped some way, but
                        // is not specific, so we have to do this.
                        // Other than this case,
                        // we should not be decoding anything since UTF-8 is the encoding set in the response.
                        getString = getString + "&" + args[i][0] + "=" + encode(args[i][1]);
                    } catch (UnsupportedEncodingException e) {
                        throw new GeneralException("Error encoding argument", e);
                    }
                }
            }
        }
        //HttpGet httpGet = new HttpGet(getString);
        HttpPost httpGet = new HttpPost(getString);
        //httpGet.setHeader("Accept", "application/json");
        httpGet.setHeader("Content-type", "application/x-www-form-urlencoded");
        HttpClient client = clientPool.pop();
        HttpResponse response = null;
        try {
            response = client.execute(httpGet);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                clientPool.doDestroy(client);
                throw new NFWException("Error: the DB service failed with status code " + response.getStatusLine());
            }
            clientPool.push(client);

            return getSerializer().deserializeToMap(response.getEntity().getContent());
        } catch (IOException e) {
            throw new GeneralException("Error invoking http client", e);
        }
    }


    public static String encode(String x) throws UnsupportedEncodingException {
        String xx = URLEncoder.encode(x, DBServiceSerializer.UTF8_ENCODING);
        return xx;
    }


    protected String[][] convertIdpsToArray(List<IdentityProvider> idps) throws UnsupportedEncodingException {
        String[][] out = new String[idps.size()][2];
        for (int i = 0; i < idps.size(); i++) {
            out[i][0] = idpKeys.identifier();
            out[i][1] = idps.get(i).getIdentifier().toString();
        }
        return out;
    }


    protected UserKeys userKeys = new UserKeys();
    protected IDPKeys idpKeys = new IDPKeys();
    protected TwoFactorSerializationKeys tfKeys = new TwoFactorSerializationKeys();
    protected ClientKeys clientKeys = new ClientKeys();
    protected ClientApprovalKeys clientApprovalKeys = new ClientApprovalKeys();

    public DBServiceSerializer getSerializer() {
        if (serializer == null) {
            serializer = new DBServiceSerializer(userKeys, idpKeys, tfKeys, clientKeys, clientApprovalKeys);
        }
        return serializer;
    }


    DBServiceSerializer serializer;

    public XMLMap getUser(UserMultiID umk, String idp) {
        return doGet(AbstractDBService.GET_USER, createUserRequest(umk, idp));
    }

    /**
     * Takes a long list of Strings and turns it into a string array suitable for the {@link #doGet(String, String[][])}
     * method. This assumes an array list of strings of the form key1,value1, key2,value2,... and returns a 2xn
     * array of the form {{key1,value},{key2,value2},...}
     *
     * @param arg
     * @return
     */
    protected String[][] pairwiseStringArray(ArrayList<String> arg) {
        int size = arg.size();
        if (0 != (size % 2)) {
            throw new IllegalArgumentException("Error: the array length must be divisible by 2");
        }
        String[][] rc = new String[size / 2][2];
        for (int i = 0; i < size / 2; i++) {
            rc[i][0] = arg.get(2 * i);
            rc[i][1] = arg.get(2 * i + 1);
        }
        return rc;
    }

    boolean isEmpty(String x) {
        return x == null || 0 == x.length();
    }

    protected String[][] createUserRequest(UserMultiID umk,
                                           String idp) {
        return createUserRequest(umk, idp, null, null, null, null, null, null, null);

    }

    protected String[][] createRequest(XMLMap map) {
        ArrayList<String> arrayList = new ArrayList<>();
        for (String key : map.keySet()) {
            Object value = map.get(key);
            if (value != null) {
                arrayList.add(key);
                if (value instanceof PersonName) {
                    arrayList.add(((PersonName) value).getName());
                } else {
                    arrayList.add(value.toString());
                }
            }
        }
        return pairwiseStringArray(arrayList);

    }

    /**
     * Create a request from whatever information is provided.
     *
     * @param umk
     * @param idp
     * @param idpDisplayName
     * @param firstName
     * @param lastName
     * @param email
     * @return
     */
    protected String[][] createUserRequest(UserMultiID umk,
                                           String idp,
                                           String idpDisplayName,
                                           String firstName,
                                           String lastName,
                                           String email,
                                           String affilation,
                                           String displayName,
                                           String organizationalUnit) {
        ArrayList<String> arrayList = new ArrayList<>();
        if (umk.hasRemoteUser()) {
            arrayList.add(userKeys.remoteUser());
            arrayList.add(umk.getRemoteUserName().getName());
        }
        if (umk.hasEPTID()) {
            arrayList.add(userKeys.eptid());
            arrayList.add(umk.getEptid().getName());
        }
        if (umk.hasEPPN()) {
            arrayList.add(userKeys.eppn());
            arrayList.add(umk.getEppn().getName());
        }
        if (umk.hasOpenID()) {
            arrayList.add(userKeys.openID());
            arrayList.add(umk.getOpenID().getName());
        }
        if (umk.hasOpenIDConnect()) {
            arrayList.add(userKeys.oidc());
            arrayList.add(umk.getOpenIDConnect().getName());
        }

        if (!isEmpty(idp)) {
            arrayList.add(userKeys.idp());
            arrayList.add(idp);
        }
        if (!isEmpty(idpDisplayName)) {
            arrayList.add(userKeys.idpDisplayName());
            arrayList.add(idpDisplayName);
        }
        if (!isEmpty(firstName)) {
            arrayList.add(userKeys.firstName());
            arrayList.add(firstName);
        }
        if (!isEmpty(lastName)) {
            arrayList.add(userKeys.lastName());
            arrayList.add(lastName);
        }
        if (!isEmpty(email)) {
            arrayList.add(userKeys.email());
            arrayList.add(email);
        }
        if (!isEmpty(affilation)) {
            arrayList.add(userKeys.affiliation());
            arrayList.add(affilation);
        }
        if (!isEmpty(displayName)) {
            arrayList.add(userKeys.displayName());
            arrayList.add(displayName);
        }
        if (!isEmpty(organizationalUnit)) {
            arrayList.add(userKeys.organizationalUnit());
            arrayList.add(organizationalUnit);
        }
        return pairwiseStringArray(arrayList);
    }

    public XMLMap updateUser(UserMultiID umk,
                             String idp,
                             String idpDisplayName,
                             String firstName,
                             String lastName,
                             String email,
                             String affiliation,
                             String displayName,
                             String organizationalUnit
    ) throws IOException {
        return doGet(AbstractDBService.UPDATE_USER, createUserRequest(umk, idp, idpDisplayName, firstName, lastName, email, affiliation, displayName, organizationalUnit));
    }


    /**
     * Convenience method to get a user with new information. This is, in effect, an update method if the
     * user changes.
     *
     * @param user
     * @return
     */
    public XMLMap getUser(User user) {
        return getUser(user.getUserMultiKey(),
                user.getIdP(),
                user.getIDPName(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getAffiliation(),
                user.getDisplayName(),
                user.getOrganizationalUnit());
    }

    public XMLMap getUser(UserMultiID umk,
                          String idp,
                          String idpDisplayName,
                          String firstName,
                          String lastName,
                          String email,
                          String affiliation,
                          String displayName,
                          String organizationalUnit) {
        return doGet(AbstractDBService.GET_USER, createUserRequest(umk, idp, idpDisplayName, firstName, lastName, email, affiliation, displayName, organizationalUnit));
    }

    public XMLMap getUser(Identifier userUid) {
        return doGet(AbstractDBService.GET_USER, new String[][]{{userKeys.identifier(), userUid.toString()}});
    }

    public XMLMap createUser(UserMultiID umk,
                             String idp,
                             String idpDisplayName,
                             String firstName,
                             String lastName,
                             String email,
                             String affiliation,
                             String displayName,
                             String organizationalUnit) {
        return doGet(AbstractDBService.CREATE_USER, createUserRequest(umk, idp, idpDisplayName, firstName, lastName, email, affiliation, displayName, organizationalUnit));
    }

    public void setTwoFactorInfo(TwoFactorInfo tfi) {
        doGet(AbstractDBService.SET_TWO_FACTOR_INFO,
                new String[][]{
                        {tfKeys.identifier(), tfi.getIdentifierString()},
                        {tfKeys.info(), tfi.getInfo()}});
    }

    public TwoFactorInfo getTwoFactorInfo(Identifier userUid) {
        XMLMap map = doGet(AbstractDBService.GET_TWO_FACTOR_INFO, new String[][]{{tfKeys.identifier(), userUid.toString()}});
        TwoFactorInfo tfi = new TwoFactorInfo(map.getIdentifier(tfKeys.identifier()), map.getString(tfKeys.info()));
        return tfi;
    }

    public Identifier getUserId(UserMultiID umk, String idp) throws IOException {
        Map<String, Object> map = getUser(umk, idp);
        return BasicIdentifier.newID(map.get(userKeys.identifier()).toString());
    }


    public boolean removeUser(Identifier userUid) {
        Map m = doGet(AbstractDBService.REMOVE_USER, new String[][]{{userKeys.identifier(), userUid.toString()}});
        Long rc = (Long) m.get(AbstractDBService.STATUS_KEY);
        if (rc == StatusCodes.STATUS_OK || rc == StatusCodes.STATUS_USER_NOT_FOUND) {
            return true;
        }
        return false;
    }

    public boolean hasUser(Identifier id) {
        try {
            Map m = doGet(AbstractDBService.HAS_USER, new String[][]{{userKeys.identifier(), id.toString()}});
            return (Long) m.get(AbstractDBService.STATUS_KEY) == StatusCodes.STATUS_USER_EXISTS;
        } catch (Exception x) {
        }
        return false;

    }

    public boolean hasUser(UserMultiID umk, String idp) {
        try {
            Map m = doGet(AbstractDBService.HAS_USER, createUserRequest(umk, idp));
            return (Long) m.get(AbstractDBService.STATUS_KEY) == StatusCodes.STATUS_USER_EXISTS;
        } catch (Exception x) {
        }
        return false;
    }

    public XMLMap addIdps(List<IdentityProvider> idps) throws IOException {
        return doGet(AbstractDBService.SET_ALL_IDPS, convertIdpsToArray(idps));
    }


    public List<IdentityProvider> getAllIdps() {
        Map<String, Object> map = doGet(AbstractDBService.GET_ALL_IDPS, new String[][]{{}});
        if(map.containsKey(idpKeys.identifier())) {
            return (List<IdentityProvider>) map.get(idpKeys.identifier());
        }
        return new ArrayList<>(); // so it's never null
    }

    public XMLMap getLastArchivedUser(Identifier userUid) {
        return doGet(AbstractDBService.GET_LAST_ARCHIVED_USER, new String[][]{{userKeys.identifier(), userUid.toString()}});
    }

    public XMLMap getPortalParameters(String token) {
        return doGet(AbstractDBService.GET_PORTAL_PARAMETER, new String[][]{{tokenKey, token}});
    }
}
