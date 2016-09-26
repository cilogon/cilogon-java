package org.cilogon.d2.util;


import edu.uiuc.ncsa.security.core.Initializable;

/**
 * An interface for things that need to return a next value atomically.
 * <p>Created by Jeff Gaynor<br>
 * on May 10, 2010 at  10:50:54 AM
 */
public interface Incrementable extends Initializable {
    public long nextValue();

    /**
     * This takes the place (or should) of {@link edu.uiuc.ncsa.security.core.Initializable#createNew()}
     * and passes along the first value of the newly created sequence. Using this method without a value
     * takes whatever the underlying implementation deems fit for a first value should be.
     * @param initialValue
     * @return
     */
    public boolean createNew(long initialValue);
}
