/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event.aspect;

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
     * x    | y                  | y
     * x+1  | 2y                 | y
     * x+2  | 3y                 | y
     * </pre>
     *
     * @return whether the counter aspect is cumulative or not
     */
    default boolean isCumulative() {
        return false;
    }

}
