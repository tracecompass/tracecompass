/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.datastore.core.interval;

import org.eclipse.tracecompass.internal.provisional.datastore.core.serialization.ISafeByteBufferReader;

/**
 * A factory that reads object from a byte buffer and create a new object
 *
 * @author Geneviève Bastien
 * @param <E>
 *            The type of objects that will be read
 */
@FunctionalInterface
public interface IHTIntervalReader<E extends IHTInterval> {

    /**
     * Method to deserialize segments to disk for Segment History Tree
     *
     * @param buffer
     *            HTNode buffer to read from
     * @return the Segment read from the buffer
     */
    E readInterval(ISafeByteBufferReader buffer);
}
