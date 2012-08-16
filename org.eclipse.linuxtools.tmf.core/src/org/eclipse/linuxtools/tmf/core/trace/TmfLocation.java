/*******************************************************************************
 * Copyright (c) 2009, 2010, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Updated as per TMF Trace Model 1.0
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace;

import java.lang.reflect.Method;

/**
 * A convenience implementation on of ITmfLocation. The generic class (L) must
 * be comparable.
 *
 * @param <L> The trace lcoation type
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfLocation<L extends Comparable<L>> implements ITmfLocation<L>, Cloneable {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private L fLocationData;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor (for the 'null' location)
     */
    @SuppressWarnings("unused")
    private TmfLocation() {
    }

    /**
     * Standard constructor.
     *
     * @param locationData the trace location
     */
    public TmfLocation(final L locationData) {
        fLocationData = locationData;
    }

    /**
     * Copy constructor
     *
     * @param location the original location
     */
    public TmfLocation(final TmfLocation<L> location) {
        fLocationData = location.fLocationData;
    }

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * @since 2.0
     */
    @Override
    public L getLocationData() {
        return fLocationData;
    }

    // ------------------------------------------------------------------------
    // Cloneable
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    @SuppressWarnings("unchecked")
    public TmfLocation<L> clone() {
        TmfLocation<L> clone = null;
        try {
            clone = (TmfLocation<L>) super.clone();
            if (fLocationData != null) {
                final Class<?> clazz = fLocationData.getClass();
                final Method method = clazz.getMethod("clone", new Class[0]); //$NON-NLS-1$
                final Object copy = method.invoke(this.fLocationData, new Object[0]);
                clone.fLocationData = (L) copy;
            } else {
                clone.fLocationData = null;
            }
        } catch (final CloneNotSupportedException e) {
        } catch (final NoSuchMethodException e) {
        } catch (final Exception e) {
            throw new InternalError(e.toString());
        }
        return clone;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fLocationData != null) ? fLocationData.hashCode() : 0);
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TmfLocation<L> other = (TmfLocation<L>) obj;
        if (fLocationData == null) {
            if (other.fLocationData != null) {
                return false;
            }
        } else if (!fLocationData.equals(other.fLocationData)) {
            return false;
        }
        return true;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "TmfLocation [fLocation=" + fLocationData + "]";
    }

}
