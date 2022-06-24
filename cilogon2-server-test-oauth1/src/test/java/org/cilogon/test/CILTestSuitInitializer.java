package org.cilogon.test;

import edu.uiuc.ncsa.myproxy.oa4mp.AbstractTestSuiteInitializer;
import edu.uiuc.ncsa.myproxy.oa4mp.TestStoreProviderInterface;
import edu.uiuc.ncsa.myproxy.oa4mp.server.ServiceEnvironment;
import edu.uiuc.ncsa.myproxy.oa4mp.server.servlet.AbstractBootstrapper;
import edu.uiuc.ncsa.security.core.util.ConfigurationLoader;
import edu.uiuc.ncsa.security.core.util.DebugUtil;
import org.cilogon.d2.CILTestStoreProvider;
import org.cilogon.d2.RemoteDBServiceTest;
import org.cilogon.d2.ServiceTestUtils;
import org.cilogon.oauth1.loader.CILogonConfigurationLoader;

import static edu.uiuc.ncsa.myproxy.oa4mp.TestUtils.findConfigNode;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 7/17/18 at  4:55 PM
 */
public class CILTestSuitInitializer extends AbstractTestSuiteInitializer  {

    public CILTestSuitInitializer(AbstractBootstrapper bootstrapper) {
        super(bootstrapper);
    }

    @Override
    public String getAggregateStoreConfigName() {
        return "cilogon.oa1.mysql";
    }

    @Override
    public TestStoreProviderInterface getTSP(final String namedNode)  {
        return new CILTestStoreProvider() {
            CILogonConfigurationLoader loader;

            @Override
            public ConfigurationLoader<? extends ServiceEnvironment> getConfigLoader() {
                if (loader == null) {
                    loader = new CILogonConfigurationLoader(findConfigNode(namedNode));
                }
                return loader;
            }

        };
    }

    @Override
    public String getFileStoreConfigName() {
        return "cilogon.cil2.fileStore";
    }

    @Override
    public String getMemoryStoreConfigName() {
        return "cilogon.cil2.memory";
    }

    @Override
    public String getMySQLStoreConfigName() {
        return "cilogon.cil2.mysql";
    }

    @Override
    public String getPostgresStoreConfigName() {
        return "cilogon.cil2.postgres";
    }

    @Override
    public String getDerbyStoreConfigName() {
        return null;
    }

    @Override
    public void init() {
        DebugUtil.setIsEnabled(true);
        RemoteDBServiceTest.setHost("http://localhost:44444/oauth/dbService");
        ServiceTestUtils.setBootstrapper(getBootstrapper());
        ServiceTestUtils.setMySQLStoreProvider(getTSP(getMySQLStoreConfigName()));
        ServiceTestUtils.setMemoryStoreProvider(getTSP(getMemoryStoreConfigName()));
        ServiceTestUtils.setFsStoreProvider(getTSP(getFileStoreConfigName()));
        ServiceTestUtils.setPgStoreProvider(getTSP(getPostgresStoreConfigName()));
        ServiceTestUtils.setAgStoreProvider(getTSP(getAggregateStoreConfigName()));
    }
}
