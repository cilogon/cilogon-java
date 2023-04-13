package org.cilogon.oauth2.servlet.storage.transaction;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/25/12 at  3:35 PM
 */
public class CILPGTransactionKeys extends CILTransactionKeys {
    /**
     * CILogon defaults for compatibility with version 1.0.
     */
    public CILPGTransactionKeys() {
        identifier("temp_cred");
        tempCred("temp_cred");
        tempCredSS("temp_cred_ss");
        tempCredValid("temp_cred_ok");
        accessTokenValid("access_token_ok");
        callbackUri("callback_uri");
        verifier("verifier");
        lifetime("lifetime");
        username("userid");
    }


}
