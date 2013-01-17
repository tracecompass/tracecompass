/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.synchronization;

import java.io.Serializable;

import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;

/**
 * A default simple, identity timestamp transform. It is a singleton class and
 * returns the timestamp itself
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class TmfTimestampTransform implements ITmfTimestampTransform, Serializable {

    /**
     * Generated serial UID
     */
    private static final long serialVersionUID = -1480581417493073304L;

    /**
     * The unique instance of this transform, since it is always the same
     */
    public static final TmfTimestampTransform IDENTITY = new TmfTimestampTransform();

    /**
     * Default constructor
     */
    protected TmfTimestampTransform() {

    }

    @Override
    public ITmfTimestamp transform(ITmfTimestamp timestamp) {
        return timestamp;
    }

    @Override
    public long transform(long timestamp) {
        return timestamp;
    }

    @Override
    public ITmfTimestampTransform composeWith(ITmfTimestampTransform composeWith) {
        /* Since this transform will not modify anything, return the other */
        return composeWith;
    }

    @Override
    public boolean equals(Object other) {
        return other.getClass().equals(TmfTimestampTransform.class);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + TmfTimestampTransform.class.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TmfTimestampTransform [ IDENTITY ]"; //$NON-NLS-1$
    }

}
