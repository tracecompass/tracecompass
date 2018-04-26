/*******************************************************************************
 * Copyright (c) 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph;

/**
 * An interface for entry model whose corresponding row states have value that
 * are between a minimum and maximum value. An entry that implements this
 * interface gives a hint that, at visualization time, the states may be
 * weighted to their value between min and max.
 *
 * @author Geneviève Bastien
 */
public interface ITimeGraphEntryModelWeighted extends ITimeGraphEntryModel {

    /**
     * Get the minimum value the states can have
     *
     * @return The minimum value of the states
     */
    long getMin();

    /**
     * Get the maximum value the states can have
     *
     * @return The maximal value of the states
     */
    long getMax();

    /**
     * Get the weighting
     *
     * @param value
     *            The value to weight
     * @return The weight of this value, with the minimum and maximum values
     */
    default double getWeight(long value) {
        return (double) (value - getMin()) / (getMax() - getMin());
    }

}
