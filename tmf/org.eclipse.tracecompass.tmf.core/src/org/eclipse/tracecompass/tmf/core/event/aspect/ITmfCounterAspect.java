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
}
