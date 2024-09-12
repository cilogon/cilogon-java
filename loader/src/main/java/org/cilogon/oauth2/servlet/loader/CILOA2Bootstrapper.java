package org.cilogon.oauth2.servlet.loader;

import edu.uiuc.ncsa.security.core.Identifiable;
import edu.uiuc.ncsa.security.core.exceptions.MyConfigurationException;
import edu.uiuc.ncsa.security.core.util.ConfigurationLoader;
import edu.uiuc.ncsa.security.servlet.Initialization;
import edu.uiuc.ncsa.security.storage.MonitoredStoreInterface;
import edu.uiuc.ncsa.security.storage.events.LastAccessedEventListener;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.cilogon.oauth2.servlet.util.DNUtil;
import org.oa4mp.delegation.common.storage.clients.Client;
import org.oa4mp.delegation.common.storage.clients.ClientConverter;
import org.oa4mp.server.api.admin.adminClient.AdminClientStoreProviders;
import org.oa4mp.server.api.admin.things.SATFactory;
import org.oa4mp.server.api.storage.servlet.AbstractBootstrapper;
import org.oa4mp.server.loader.oauth2.OA2SE;
import org.oa4mp.server.loader.oauth2.loader.OA2ServletInitializer;
import org.oa4mp.server.loader.oauth2.cm.oidc_cm.OIDCCMServlet;

import javax.servlet.ServletException;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/20/12 at  3:06 PM
 */
public class CILOA2Bootstrapper extends AbstractBootstrapper {
    public static final String CIL_CONFIG_FILE_KEY = "oa4mp:cil-oa2.server.config.file";
    public static final String CIL_CONFIG_NAME_KEY = "oa4mp:cil-oa2.server.config.name";

    @Override
    public String getOa4mpConfigNameKey() {
        return CIL_CONFIG_NAME_KEY;
    }

    @Override
    public String getOa4mpConfigFileKey() {
        return CIL_CONFIG_FILE_KEY;
    }

    @Override
    public ConfigurationLoader getConfigurationLoader(ConfigurationNode node) throws MyConfigurationException {
        return new CILOA2ConfigurationLoader<OA2SE>(node);
    }

    public static class CILOA2ServletInitializer extends OA2ServletInitializer {
        @Override
        public void init() throws ServletException {
            CILogonOA2ServiceEnvironment se = (CILogonOA2ServiceEnvironment) getEnvironment();
            // Setting up CM defaults so they don't end up in the configuration.
            // CILogon policy is no refresh tokens unless explicitly ok'd.
            se.getCmConfigs().getRFC7591Config().setDefaultRefreshTokenLifetime(0L);
            se.getCmConfigs().getRFC7592Config().setDefaultRefreshTokenLifetime(0L);
            // CIL-1975. For version 5.5., keep CILogon at 5.4 so COmanage has time to
            // catch up.
            OIDCCMServlet.setDefaultAPIVersion(OIDCCMServlet.API_VERSION_5_4);
            try {
                SATFactory.setAdminClientConverter(AdminClientStoreProviders.getAdminClientConverter());
                SATFactory.setClientConverter((ClientConverter<? extends Client>) se.getClientStore().getMapConverter());
            } catch (Exception e) {
                e.printStackTrace();
            }
            DNUtil.setComputeFNAL(se.isComputeFNAL());
          // NOTE for CILogon the default is to enable cleanup locking.
            se.setCleanupLockingEnabled(true);
           super.init();
        }

        @Override
        protected void addMonitoredStores(OA2SE oa2SE, LastAccessedEventListener lastAccessedEventListener) {
            super.addMonitoredStores(oa2SE, lastAccessedEventListener);
            CILogonOA2ServiceEnvironment ciloa2 = (CILogonOA2ServiceEnvironment)oa2SE;
            ((MonitoredStoreInterface<Identifiable>) (ciloa2.getUserStore())).addLastAccessedEventListener(lastAccessedEventListener);
            ((MonitoredStoreInterface<Identifiable>) (ciloa2.getIDPStore())).addLastAccessedEventListener(lastAccessedEventListener);
        }
    }

    @Override
    public Initialization getInitialization() {
        return new CILOA2ServletInitializer();
    }
}
