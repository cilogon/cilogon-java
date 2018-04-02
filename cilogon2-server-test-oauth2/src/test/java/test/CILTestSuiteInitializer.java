package test;

import edu.uiuc.ncsa.myproxy.oa4mp.TestUtils;
import edu.uiuc.ncsa.myproxy.oa4mp.server.admin.adminClient.AdminClientStoreProviders;
import edu.uiuc.ncsa.myproxy.oa4mp.server.admin.things.SATFactory;
import edu.uiuc.ncsa.myproxy.oa4mp.server.servlet.AbstractBootstrapper;
import edu.uiuc.ncsa.myproxy.oa4mp.server.servlet.AbstractConfigurationLoader;
import edu.uiuc.ncsa.security.delegation.storage.Client;
import edu.uiuc.ncsa.security.delegation.storage.impl.ClientConverter;
import org.cilogon.oauth2.servlet.loader.CILOA2ConfigurationLoader;

import static edu.uiuc.ncsa.myproxy.oa4mp.TestUtils.findConfigNode;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/18/17 at  4:17 PM
 */
public class CILTestSuiteInitializer extends TestSuiteInitializer {

    public CILTestSuiteInitializer(AbstractBootstrapper bootstrapper) {
        super(bootstrapper);
    }

    public CILTestStoreProvider2 getTSP(final String namedNode) {
        return new CILTestStoreProvider2() {
            CILOA2ConfigurationLoader loader;

            @Override
            public AbstractConfigurationLoader getConfigLoader() {
                if (loader == null) {
                    loader = new CILOA2ConfigurationLoader(findConfigNode(namedNode));
                }
                return loader;
            }
        };
    }

    public void init() {
         TestUtils.setBootstrapper(getBootstrapper());
        // Remember that the way this works is to get the test store provider with the given name in the
        // configuration file.
         TestUtils.setMemoryStoreProvider(getTSP("cil-oa2.test.memory"));
         TestStoreProvider2 fsp = getTSP("cil-oa2.test.fileStore"); // use this later to get its client converter. Any store would do.
         TestUtils.setFsStoreProvider(fsp);
         TestUtils.setMySQLStoreProvider(getTSP("cil-oa2.test.mysql"));
         TestUtils.setPgStoreProvider(getTSP("cil-oa2.test.postgres"));
   //      TestUtils.setAgStoreProvider(new AGTestStoreProvider("cil-oa2.test.fileStore"));

         //TestUtils.setH2StoreProvider(getTSP(""h2-oa2");
         //TestUtils.setDerbyStoreProvider(getTSP(""derby-oa2");

         try {
             SATFactory.setAdminClientConverter(AdminClientStoreProviders.getAdminClientConverter());
             SATFactory.setClientConverter((ClientConverter<? extends Client>) fsp.getClientStore().getACConverter());
         } catch (Exception e) {
             e.printStackTrace();
         }
     }

}
