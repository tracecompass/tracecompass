/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.synchronization;

import java.io.Serializable;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;

/**
 * This class contains a formula to transform the value of a timestamp, for
 * example after trace synchronization
 *
 * @author Geneviève Bastien
 */
public interface ITmfTimestampTransform extends Serializable {

    /**
     * Transforms a timestamp
     *
     * @param timestamp
     *            The timestamp to transform
     * @return the transformed timestamp
     */
    @NonNull ITmfTimestamp transform(@NonNull ITmfTimestamp timestamp);

    /**
     * Transforms a timestamp value
     *
     * @param timestamp
     *            The timestamp to transform in nanoseconds
     * @return the transformed value
     */
    long transform(long timestamp);

    /**
     * Returns a timestamp transform that is the composition of two timestamp
     * transforms. Composed objects must be the same type.
     *
     * @param composeWith
     *            The transform to first apply on the timestamp before applying
     *            the current object
     * @return A new timestamp transform object with the resulting composition.
     *
     * TODO: allow composition of different transform types.
     */
    ITmfTimestampTransform composeWith(ITmfTimestampTransform composeWith);

}
