package org.cilogon.oauth1.loader;

import edu.uiuc.ncsa.myproxy.oa4mp.server.MyProxyFacadeProvider;
import edu.uiuc.ncsa.myproxy.oa4mp.server.ServiceEnvironmentImpl;
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
import edu.uiuc.ncsa.security.servlet.UsernameTransformer;
import edu.uiuc.ncsa.security.util.mail.MailUtilProvider;
import org.cilogon.d2.storage.IdentityProviderStore;
import org.cilogon.d2.storage.UserStore;
import org.cilogon.d2.twofactor.TwoFactorStore;
import org.cilogon.d2.util.ArchivedUserStore;
import org.cilogon.d2.util.CILogonSE;
import org.cilogon.d2.util.CILogonSEImpl;
import org.cilogon.d2.util.Incrementable;

import javax.inject.Provider;
import java.util.HashMap;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/30/15 at  11:37 AM
 */
public class CILogonServiceEnviroment extends ServiceEnvironmentImpl implements CILogonSE {
    public CILogonServiceEnviroment(MyLoggingFacade logger,
                                    Provider<TransactionStore> tsp,
                                    Provider<ClientStore> csp,
                                    int maxAllowedNewClientRequests,
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
                                    Provider<UserStore> ups,
                                    Provider<ArchivedUserStore> ausp,
                                    Provider<IdentityProviderStore> idpsp,
                                    Provider<Incrementable> incp,
                                    Provider<TwoFactorStore> tfsp,
                                    boolean computeFNAL,
                                    Provider<PermissionsStore> permissionStoreProvider) {
        super(logger,
                mfp,
                tsp,
                csp,
                maxAllowedNewClientRequests,
                casp,
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
                permissionStoreProvider);
        ciLogonSE = new CILogonSEImpl(ups, ausp, idpsp, incp, tfsp, computeFNAL);
    }

    CILogonSEImpl ciLogonSE = null;

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

    @Override
    public boolean isPrintTSInDebug() {
        return ciLogonSE.isPrintTSInDebug();
    }
}
