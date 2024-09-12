package test;

import org.oa4mp.delegation.common.storage.clients.Client;
import org.oa4mp.delegation.common.storage.clients.ClientConverter;
import org.oa4mp.server.api.admin.adminClient.AdminClientStoreProviders;
import org.oa4mp.server.api.admin.things.SATFactory;
import org.oa4mp.server.api.storage.servlet.AbstractBootstrapper;
import org.oa4mp.server.api.storage.servlet.AbstractConfigurationLoader;
import org.oa4mp.server.test.AbstractTestSuiteInitializer;
import org.oa4mp.server.test.TestStoreProvider2;
import test.cilogon.RemoteDBServiceTest;
import test.cilogon.ServiceTestUtils;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/18/17 at  4:17 PM
 */
public class CILTestSuiteInitializer2 extends AbstractTestSuiteInitializer {


    public CILTestSuiteInitializer2(AbstractBootstrapper bootstrapper) {
        super(bootstrapper);
    }

    public CILTestStoreProvider2 getTSP(final String namedNode) {
       CILTestStoreProviderImpl CILTestStoreProviderImpl = new CILTestStoreProviderImpl(namedNode);
        return new CILTestStoreProvider2(CILTestStoreProviderImpl) {

            @Override
            public AbstractConfigurationLoader getConfigLoader() {
               return (AbstractConfigurationLoader) getCilTSP().getConfigLoader();
            }
        };
    }

    @Override
    public String getAggregateStoreConfigName() {
        return null;
    }

    @Override
    public String getFileStoreConfigName() {
        return null;
    }

    @Override
    public String getMemoryStoreConfigName() {
        return null;
    }

    @Override
    public String getMySQLStoreConfigName() {
        return null;
    }

    @Override
    public String getPostgresStoreConfigName() {
        return null;
    }

    @Override
    public String getDerbyStoreConfigName() {
        return null;
    }

    public void init() {
        RemoteDBServiceTest.setHost(RemoteDBServiceTest.getHost());
        ServiceTestUtils.setBootstrapper(getBootstrapper());
        // Remember that the way this works is to get the test store provider with the given name in the
        // configuration file.
        ServiceTestUtils.setMemoryStoreProvider(getTSP("cil-oa2.test.memory"));
        TestStoreProvider2 fsp = getTSP("cil-oa2.test.fileStore"); // use this later to get its client converter. Any store would do.
        ServiceTestUtils.setFsStoreProvider(fsp);
        ServiceTestUtils.setMySQLStoreProvider(getTSP("cil-oa2.test.mysql"));
        ServiceTestUtils.setPgStoreProvider(getTSP("cil-oa2.test.postgres"));
        ServiceTestUtils.setPgStoreProvider(getTSP("cil-oa2.test.postgres"));
        ServiceTestUtils.setDerbyStoreProvider(getTSP("cil-oa2.test.derby"));
        try {
            SATFactory.setAdminClientConverter(AdminClientStoreProviders.getAdminClientConverter());
            SATFactory.setClientConverter((ClientConverter<? extends Client>) fsp.getClientStore().getMapConverter());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
