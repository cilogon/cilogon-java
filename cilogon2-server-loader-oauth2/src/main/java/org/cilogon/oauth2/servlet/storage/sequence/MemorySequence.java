package org.cilogon.oauth2.servlet.storage.sequence;

import edu.uiuc.ncsa.security.core.exceptions.UninitializedException;
import org.cilogon.oauth2.servlet.util.Incrementable;

/**
 * Simple in-memory sequence.
 * <p>Created by Jeff Gaynor<br>
 * on 3/13/12 at  2:23 PM
 */
public class MemorySequence implements Incrementable {
    public MemorySequence() {
        this.startValue = DEFAULT_START_VALUE;
    }

    static long DEFAULT_START_VALUE = 42L;
    public MemorySequence(long startValue) {
        this.startValue = startValue;
    }

    long startValue = DEFAULT_START_VALUE;
    @Override
    public long nextValue() {
        checkDestroyed();
        return startValue++;
    }

    @Override
    public boolean createNew(long initialValue) {
        startValue = initialValue;
        return true;
    }

    boolean destroyed = false;
    @Override
    public boolean destroy() {
        destroyed = true;
        return destroyed;
    }

    @Override
    public boolean init() {
        startValue =DEFAULT_START_VALUE;
        return true;
    }

    @Override
    public boolean createNew() {
        startValue = DEFAULT_START_VALUE;
        return true;
    }

    @Override
    public boolean isCreated() {
        if(destroyed) return false;
        return true;
    }

    @Override
    public boolean isInitialized() {
        return true;
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }
    protected void checkDestroyed(){
        if(destroyed) throw new UninitializedException("Store was destroyed. Recreate before use.");
    }
}
