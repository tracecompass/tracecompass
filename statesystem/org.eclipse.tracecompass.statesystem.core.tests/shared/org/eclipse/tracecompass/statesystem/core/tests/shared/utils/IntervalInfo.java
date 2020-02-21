/*******************************************************************************
 * Copyright (c) 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.tests.shared.utils;

import java.util.List;

import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;

/**
 * Class to group an attribute path and its intervals
 *
 * @author Geneviève Bastien
 */
public class IntervalInfo {

    private final String[] fAttributePath;
    private final List<ITmfStateInterval> fIntervals;

    /**
     * Constructor
     *
     * @param intervals
     *            The list of intervals for the full time range of the
     *            attribute
     * @param attributePath
     *            The attribute path
     */
    public IntervalInfo(List<ITmfStateInterval> intervals, String... attributePath) {
        fAttributePath = attributePath;
        fIntervals = intervals;
    }

    /**
     * Get the attribute path
     *
     * @return The attribute path
     */
    public String[] getAttributePath() {
        return fAttributePath;
    }

    /**
     * Get the list of intervals
     *
     * @return The list of intervals
     */
    public List<ITmfStateInterval> getIntervals() {
        return fIntervals;
    }

}
