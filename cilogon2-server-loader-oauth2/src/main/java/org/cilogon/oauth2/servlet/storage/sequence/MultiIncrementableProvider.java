package org.cilogon.oauth2.servlet.storage.sequence;

import edu.uiuc.ncsa.security.core.configuration.provider.MultiTypeProvider;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.cilogon.oauth2.servlet.util.Incrementable;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/20/12 at  9:39 AM
 */
public class MultiIncrementableProvider extends MultiTypeProvider<Incrementable> {
    long initialValue = 42L;

    public MultiIncrementableProvider(ConfigurationNode cn ,
                                      boolean disableDefaultStore,
                                      MyLoggingFacade logger, long initialValue) {
        super(cn, disableDefaultStore, logger, null, null);
        this.initialValue = initialValue;
    }

    MemorySequence defaultSeq;
    @Override
    public Incrementable getDefaultStore() {
        if(defaultSeq == null){
            logger.info("Using default in memory sequence.");
            defaultSeq=new MemorySequence(initialValue);
        }
        return defaultSeq;
    }
}
