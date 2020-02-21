/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.counters.core.aspects;

import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;

/**
 * Counter aspect, used for incrementing long aspects.
 *
 * @author Matthew Khouzam
 * @author Mikael Ferland
 * @since 3.1
 */
public interface ITmfCounterAspect extends ITmfEventAspect<Long> {

    /**
     * Avoid cluttering the trace's event table if there are too many counters.
     *
     * Note: The counter aspects columns could still be visible depending on the
     * cached configuration of the event table.
     */
    @Override
    default boolean isHiddenByDefault() {
        return true;
    }

    /**
     * Indicate whether or not the counter is cumulative throughout time.
     *
     * The table below outlines the difference between a cumulative and a
     * non-cumulative counter:
     *
     * <pre>
     * Time | Cumulative counter | Non-cumulative counter
     * ==================================================
     * x    | y1                  | y1
     * x+1  | y1 + y2             | y2
     * x+2  | y1 + y2 + y3        | y3
     * </pre>
     *
     * @return whether the counter aspect is cumulative or not
     */
    default boolean isCumulative() {
        return true;
    }

}
