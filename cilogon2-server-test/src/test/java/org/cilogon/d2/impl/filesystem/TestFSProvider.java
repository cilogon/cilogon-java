package org.cilogon.d2.impl.filesystem;

import edu.uiuc.ncsa.security.core.configuration.Configurations;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.cilogon.d2.CILTestStoreProvider;


/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/13/12 at  4:02 PM
 */
public abstract class TestFSProvider extends CILTestStoreProvider {
    protected ConfigurationNode getConfiguration() {
          if (node == null) {
              XMLConfiguration cfg = Configurations.getConfiguration(getClass().getResource(getResName()));
              node = Configurations.getConfig(cfg, "service", getCfgName());
          }

          return node;
      }

    public String getCfgName() {
        return "test";
    }

    public String getResName() {
        return "/file-test.xml";
    }

}
