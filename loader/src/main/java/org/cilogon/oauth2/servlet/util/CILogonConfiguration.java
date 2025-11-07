package org.cilogon.oauth2.servlet.util;

import edu.uiuc.ncsa.security.core.Version;
import edu.uiuc.ncsa.security.core.configuration.provider.MultiTypeProvider;
import edu.uiuc.ncsa.security.core.util.IdentifiableProviderImpl;
import org.cilogon.oauth2.servlet.storage.archiveUser.ArchivedUser;
import org.cilogon.oauth2.servlet.storage.archiveUser.ArchivedUserStore;
import org.cilogon.oauth2.servlet.storage.idp.IdentityProviderStore;
import org.cilogon.oauth2.servlet.storage.sequence.SerialStringProvider;
import org.cilogon.oauth2.servlet.storage.user.User;
import org.cilogon.oauth2.servlet.storage.user.UserStore;
import org.cilogon.oauth2.servlet.storage.TokenPrefixProvider;
import org.cilogon.oauth2.servlet.storage.twofactor.TwoFactorInfo;
import org.cilogon.oauth2.servlet.storage.twofactor.TwoFactorStore;

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
    // Fix https://github.com/cilogon/cilogon-java/issues/56
    String CILOGON_VERSION_NUMBER = "6.x-SNAPSHOT";
}
