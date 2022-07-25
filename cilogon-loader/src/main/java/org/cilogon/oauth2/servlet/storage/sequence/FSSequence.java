package org.cilogon.oauth2.servlet.storage.sequence;

import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.core.exceptions.UninitializedException;
import org.apache.commons.transaction.file.FileSequence;
import org.apache.commons.transaction.file.ResourceManagerException;
import org.apache.commons.transaction.util.LoggerFacade;
import org.cilogon.oauth2.servlet.util.Incrementable;

/**
 * Simple extension to Apache's file sequence.
 * <p>Created by Jeff Gaynor<br>
 * on 3/8/12 at  10:58 AM
 */
public class FSSequence extends FileSequence implements Incrementable {
    public static final String DEFAULT_SEQUENCE_NAME = "sequence";
    public static final Long DEFAULT_SEQUENCE_INCREMENT = 1L;

    public FSSequence(String storeDir, LoggerFacade logger) throws ResourceManagerException {
        super(storeDir, logger);
        if(!exists(DEFAULT_SEQUENCE_NAME)){
            createNew();
        }
    }


    @Override
    public long nextValue() {
        checkDestroyed();
        long x = 0;
        try {
            x = nextSequenceValueBottom(DEFAULT_SEQUENCE_NAME, DEFAULT_SEQUENCE_INCREMENT);
        } catch (ResourceManagerException e) {
            throw new GeneralException("Error: could not increment file sequence.", e);
        }
        return x;
    }

    boolean destroyed = false;
    boolean created = true;
    boolean initialized = false;

    @Override
    public boolean createNew() {
        return createNew(1L);
    }

    @Override
    public boolean createNew(long initialValue) {
        try {
            delete(DEFAULT_SEQUENCE_NAME);
            created = create(DEFAULT_SEQUENCE_NAME, initialValue);
            destroyed = false;
            return false;
        } catch (ResourceManagerException rx) {
            throw new GeneralException(("Error creating new file sequence"), rx);
        }
    }

    @Override
    public boolean destroy() {
        destroyed = delete(DEFAULT_SEQUENCE_NAME);
        return destroyed;
    }

    @Override
    public boolean init() {
        destroy();
        initialized = createNew();
        destroyed = false;
        return initialized;
    }

    @Override
    public boolean isCreated() {
        return created;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    protected void checkDestroyed() {
        if (destroyed) throw new UninitializedException("Store was destroyed. Recreate before use.");
    }

}
