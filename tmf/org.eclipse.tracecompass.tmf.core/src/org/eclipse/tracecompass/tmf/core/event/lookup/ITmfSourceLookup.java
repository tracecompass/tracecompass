/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event.lookup;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Interface for events to implement to provide information for source lookup.
 *
 * @author Bernd Hufmann
 */
public interface ITmfSourceLookup {

    /**
     * Returns the call site of this event, or 'null' if there is no call site
     * information available.
     *
     * @return The call site instance
     */
    @Nullable ITmfCallsite getCallsite();

    /**
     * <p>
     * Returns a list of call sites of this event or 'null' if there is no call
     * site information available.
     * </p>
     * <p>
     * Example uses of multiple callsites can be a product of snapshots of
     * pipelines or polled events on stream processes.
     * <p>
     *
     * @return a list of the callsites returned, order is undefined, duplication
     *         is possible.
     *
     * @since 5.1
     */
    default @Nullable List<@NonNull ITmfCallsite> getCallsites() {
        ITmfCallsite callsite = getCallsite();
        if (callsite == null) {
            return null;
        }
        return Collections.singletonList(callsite);
    }
}
