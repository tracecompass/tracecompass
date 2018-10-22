/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.datastore.core.interval;

/**
 * Generic interface for any serializable object (like a time range) that can be used in the
 * generic history tree.
 *
 * @author Alexandre Montplaisir
 * @author Geneviève Bastien
 * @since 1.1
 */
public interface IHTInterval extends ISerializableObject {

    /**
     * The start position/time of the object.
     *
     * @return The start position
     */
    long getStart();

    /**
     * The end position/time of the object
     *
     * @return The end position
     */
    long getEnd();

    /**
     * Utility method to check if the current interval intersects a timestamp.
     *
     * @param timestamp
     *            The timestamp to check
     * @return If it intersects or not
     */
    default boolean intersects(long timestamp) {
        return (getStart() <= timestamp && timestamp <= getEnd());
    }

}
