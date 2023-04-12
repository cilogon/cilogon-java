package org.cilogon.oauth2.servlet.loader;

import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.OA2SE;
import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.loader.OA2ServletInitializer;
import edu.uiuc.ncsa.myproxy.oa4mp.server.admin.adminClient.AdminClientStoreProviders;
import edu.uiuc.ncsa.myproxy.oa4mp.server.admin.things.SATFactory;
import edu.uiuc.ncsa.myproxy.oa4mp.server.servlet.AbstractBootstrapper;
import edu.uiuc.ncsa.myproxy.oa4mp.server.servlet.EnvServlet;
import edu.uiuc.ncsa.oa4mp.delegation.common.storage.Client;
import edu.uiuc.ncsa.oa4mp.delegation.common.storage.impl.ClientConverter;
import edu.uiuc.ncsa.security.core.exceptions.MyConfigurationException;
import edu.uiuc.ncsa.security.core.util.ConfigurationLoader;
import edu.uiuc.ncsa.security.servlet.Initialization;
import edu.uiuc.ncsa.security.storage.ListeningStoreInterface;
import edu.uiuc.ncsa.security.storage.events.LastAccessedEventListener;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.cilogon.oauth2.servlet.util.DNUtil;

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
            EnvServlet mps = (EnvServlet) getServlet();
            CILogonOA2ServiceEnvironment se = (CILogonOA2ServiceEnvironment) getEnvironment();
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
            ((ListeningStoreInterface) (ciloa2.getUserStore())).addLastAccessedEventListener(lastAccessedEventListener);
        }
    }

    @Override
    public Initialization getInitialization() {
        return new CILOA2ServletInitializer();
    }
}
