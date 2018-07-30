package org.cilogon.oauth2.servlet.loader;

import edu.uiuc.ncsa.myproxy.oa4mp.oauth2.loader.OA2ServletInitializer;
import edu.uiuc.ncsa.myproxy.oa4mp.server.admin.adminClient.AdminClientStoreProviders;
import edu.uiuc.ncsa.myproxy.oa4mp.server.admin.things.SATFactory;
import edu.uiuc.ncsa.myproxy.oa4mp.server.servlet.AbstractBootstrapper;
import edu.uiuc.ncsa.myproxy.oa4mp.server.servlet.EnvServlet;
import edu.uiuc.ncsa.security.core.exceptions.MyConfigurationException;
import edu.uiuc.ncsa.security.core.util.ConfigurationLoader;
import edu.uiuc.ncsa.security.delegation.storage.Client;
import edu.uiuc.ncsa.security.delegation.storage.impl.ClientConverter;
import edu.uiuc.ncsa.security.servlet.Initialization;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.cilogon.d2.util.DNUtil;

import javax.servlet.ServletException;
import java.io.IOException;
import java.sql.SQLException;

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
        return new CILOA2ConfigurationLoader(node);
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

            try {
                mps.processStoreCheck(se.getUserStore());
                mps.processStoreCheck(se.getArchivedUserStore());
                mps.processStoreCheck(se.getIDPStore());
                mps.processStoreCheck(se.getTwoFactorStore());
                mps.processStoreCheck(se.getAdminClientStore());
                mps.processStoreCheck(se.getPermissionStore());
                mps.storeUpdates();

            } catch (IOException | SQLException e) {
                e.printStackTrace();
                throw new ServletException("Could not update table", e);
            }
            super.init();
        }
    }

    @Override
    public Initialization getInitialization() {
        return new CILOA2ServletInitializer();
    }
}
