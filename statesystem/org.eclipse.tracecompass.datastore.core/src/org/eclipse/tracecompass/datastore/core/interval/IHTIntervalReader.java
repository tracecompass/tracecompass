/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.datastore.core.interval;

import org.eclipse.tracecompass.datastore.core.serialization.ISafeByteBufferReader;

/**
 * A factory that reads object from a byte buffer and create a new object
 *
 * @author Geneviève Bastien
 * @param <E>
 *            The type of objects that will be read
 * @since 1.1
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
