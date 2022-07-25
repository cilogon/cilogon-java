package org.cilogon.oauth2.servlet.storage.sequence;

import edu.uiuc.ncsa.myproxy.oa4mp.server.OA4MPConfigTags;
import edu.uiuc.ncsa.security.core.exceptions.MyConfigurationException;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.transaction.file.ResourceManagerException;
import org.apache.commons.transaction.util.PrintWriterLogger;
import org.cilogon.oauth2.servlet.util.Incrementable;

import java.io.PrintWriter;

import static edu.uiuc.ncsa.security.core.configuration.Configurations.getFirstAttribute;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/20/12 at  9:55 AM
 */
public class FSSequenceProvider extends IncrementableProvider  implements OA4MPConfigTags {

    public FSSequenceProvider(ConfigurationNode config) {
        super(config, FILE_STORE);
    }

    @Override
    public Incrementable get() {
        try {
            //ConfigurationNode configurationNode = getConfigurationAt(FILE_STORE);
            ConfigurationNode configurationNode = getTypeConfig();
            String path;
            return new FSSequence(getFirstAttribute(configurationNode, FS_PATH),
                    new PrintWriterLogger(new PrintWriter(System.out), "fileSequence", true));
        } catch (ResourceManagerException e) {
            throw new MyConfigurationException("Error: Could not create file sequence", e);
        }
    }
}
