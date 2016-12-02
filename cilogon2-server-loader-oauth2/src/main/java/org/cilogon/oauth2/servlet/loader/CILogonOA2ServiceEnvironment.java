package org.cilogon.oauth2.servlet.loader;

import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.OA2SE;
import edu.uiuc.ncsa.myproxy.oa4mp.server.MyProxyFacadeProvider;
import edu.uiuc.ncsa.myproxy.oa4mp.server.admin.adminClient.AdminClientStore;
import edu.uiuc.ncsa.myproxy.oa4mp.server.admin.permissions.PermissionsStore;
import edu.uiuc.ncsa.myproxy.oa4mp.server.servlet.AuthorizationServletConfig;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import edu.uiuc.ncsa.security.delegation.server.issuers.AGIssuer;
import edu.uiuc.ncsa.security.delegation.server.issuers.ATIssuer;
import edu.uiuc.ncsa.security.delegation.server.issuers.PAIssuer;
import edu.uiuc.ncsa.security.delegation.server.storage.ClientApprovalStore;
import edu.uiuc.ncsa.security.delegation.server.storage.ClientStore;
import edu.uiuc.ncsa.security.delegation.storage.TransactionStore;
import edu.uiuc.ncsa.security.delegation.token.TokenForge;
import edu.uiuc.ncsa.security.oauth_2_0.server.LDAPConfiguration;
import edu.uiuc.ncsa.security.oauth_2_0.server.ScopeHandler;
import edu.uiuc.ncsa.security.servlet.UsernameTransformer;
import edu.uiuc.ncsa.security.util.mail.MailUtilProvider;
import org.cilogon.d2.storage.IdentityProviderStore;
import org.cilogon.d2.storage.UserStore;
import org.cilogon.d2.twofactor.TwoFactorStore;
import org.cilogon.d2.util.ArchivedUserStore;
import org.cilogon.d2.util.CILogonSE;
import org.cilogon.d2.util.CILogonSEImpl;
import org.cilogon.d2.util.Incrementable;
import org.cilogon.oauth2.servlet.impl.CILogonScopeHandler;

import javax.inject.Provider;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/30/15 at  12:00 PM
 */
public class CILogonOA2ServiceEnvironment extends OA2SE implements CILogonSE {
    public CILogonOA2ServiceEnvironment(MyLoggingFacade logger,
                                        Provider<TransactionStore> tsp,
                                        Provider<ClientStore> csp,
                                        int maxAllowedNewClientRequests,
                                        long rtLifetime,
                                        Provider<ClientApprovalStore> casp,
                                        List<MyProxyFacadeProvider> mfp,
                                        MailUtilProvider mup,
                                        MessagesProvider messagesProvider,
                                        Provider<AGIssuer> agip,
                                        Provider<ATIssuer> atip,
                                        Provider<PAIssuer> paip,
                                        Provider<TokenForge> tfp,
                                        HashMap<String, String> constants,
                                        AuthorizationServletConfig ac,
                                        UsernameTransformer usernameTransformer,
                                        boolean isPingable,
                                        int clientSecretLength,
                                        Provider<UserStore> usp,
                                        Provider<ArchivedUserStore> ausp,
                                        Provider<IdentityProviderStore> idpsp,
                                        Provider<Incrementable> incp,
                                        Provider<TwoFactorStore> tfsp,
                                        Collection<String> scopes,
                                        ScopeHandler scopeHandler,
                                        LDAPConfiguration ldapConfiguration,
                                        boolean isRefreshtokenEnabled,
                                        boolean isTwoFactorSupportEnabled,
                                        long maxClientRefreshTokenLifetime,
                                        boolean isComputeFNAL,
                                        Provider<PermissionsStore> permissionsStoreProvider,
                                        Provider<AdminClientStore> adminClientStoreProvider) {
        super(logger,
                tsp,
                csp,
                maxAllowedNewClientRequests,
                rtLifetime,
                casp,
                mfp,
                mup,
                messagesProvider,
                agip,
                atip,
                paip,
                tfp,
                constants,
                ac,
                usernameTransformer,
                isPingable,
                permissionsStoreProvider,
                adminClientStoreProvider,
                clientSecretLength,
                scopes,
                scopeHandler,
                ldapConfiguration,
                isRefreshtokenEnabled,
                isTwoFactorSupportEnabled,
                maxClientRefreshTokenLifetime);
        /*
                       boolean twoFactorSupportEnabled,
                 long maxClientRefreshTokenLifetime) {

         */
        ciLogonSE = new CILogonSEImpl(usp, ausp, idpsp, incp, tfsp, isComputeFNAL);
        if(scopeHandler instanceof CILogonScopeHandler){
            ((CILogonScopeHandler)scopeHandler).setOa2SE(this);
        }
    }


    CILogonSE ciLogonSE;

    @Override
    public ArchivedUserStore getArchivedUserStore() {
        return ciLogonSE.getArchivedUserStore();
    }

    @Override
    public TwoFactorStore getTwoFactorStore() {
        return ciLogonSE.getTwoFactorStore();
    }

    @Override
    public UserStore getUserStore() {
        return ciLogonSE.getUserStore();
    }

    @Override
    public IdentityProviderStore getIDPStore() {
        return ciLogonSE.getIDPStore();
    }

    @Override
    public Incrementable getIncrementable() {
        return ciLogonSE.getIncrementable();
    }

    @Override
    public int getMaxUserIdRetries() {
        return ciLogonSE.getMaxUserIdRetries();
    }

    @Override
    public void setMaxUserIdRetries(int maxUserIdRetries) {
        ciLogonSE.setMaxUserIdRetries(maxUserIdRetries);
    }

    @Override
    public boolean isComputeFNAL() {
        return ciLogonSE.isComputeFNAL();
    }
}
