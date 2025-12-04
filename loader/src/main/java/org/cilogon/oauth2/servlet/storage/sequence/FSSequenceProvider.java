package org.cilogon.oauth2.servlet.storage.sequence;

import edu.uiuc.ncsa.security.core.cf.CFNode;
import edu.uiuc.ncsa.security.core.exceptions.MyConfigurationException;
import org.apache.commons.transaction.file.ResourceManagerException;
import org.apache.commons.transaction.util.PrintWriterLogger;
import org.cilogon.oauth2.servlet.util.Incrementable;
import org.oa4mp.server.api.OA4MPConfigTags;

import java.io.PrintWriter;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/20/12 at  9:55 AM
 */
public class FSSequenceProvider extends IncrementableProvider  implements OA4MPConfigTags {

    public FSSequenceProvider(CFNode config) {
        super(config, FILE_STORE);
    }

    @Override
    public Incrementable get() {
        try {
            //ConfigurationNode configurationNode = getConfigurationAt(FILE_STORE);
            CFNode configurationNode = getCFNode();
            String path;
            return new FSSequence(configurationNode.getFirstAttribute(FS_PATH),
                    new PrintWriterLogger(new PrintWriter(System.out), "fileSequence", true));
        } catch (ResourceManagerException e) {
            throw new MyConfigurationException("Error: Could not create file sequence", e);
        }
    }
}
