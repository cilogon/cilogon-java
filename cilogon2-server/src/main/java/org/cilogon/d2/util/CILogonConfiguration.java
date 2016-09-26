package org.cilogon.d2.util;

import edu.uiuc.ncsa.security.core.Version;
import edu.uiuc.ncsa.security.core.configuration.provider.MultiTypeProvider;
import edu.uiuc.ncsa.security.core.util.IdentifiableProviderImpl;
import org.cilogon.d2.storage.ArchivedUser;
import org.cilogon.d2.storage.IdentityProviderStore;
import org.cilogon.d2.storage.User;
import org.cilogon.d2.storage.UserStore;
import org.cilogon.d2.storage.provider.TokenPrefixProvider;
import org.cilogon.d2.twofactor.TwoFactorInfo;
import org.cilogon.d2.twofactor.TwoFactorStore;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/26/15 at  4:17 PM
 */
public interface CILogonConfiguration extends Version {

    public SerialStringProvider getSsp();

    public TokenPrefixProvider getTokenPrefixProvider();

    public MultiTypeProvider<TwoFactorStore> getM2P();

    public MultiTypeProvider<Incrementable> getIp();

    public IdentifiableProviderImpl<TwoFactorInfo> get2fp();

    public MultiTypeProvider<UserStore> getUSP();

    public IdentifiableProviderImpl<User> getUP();

    public IdentifiableProviderImpl<ArchivedUser> getAUP();

    public MultiTypeProvider<ArchivedUserStore> getMUASP();

    public MultiTypeProvider<IdentityProviderStore> getMidp();
}
