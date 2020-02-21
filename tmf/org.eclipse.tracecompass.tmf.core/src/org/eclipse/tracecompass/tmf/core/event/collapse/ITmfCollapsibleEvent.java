/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.core.event.collapse;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * Interface for deciding whether an event can be collapsed with another event.
 *
 * @author Bernd Hufmann
 */
public interface ITmfCollapsibleEvent {

    /**
     * Verifies if an event is similar to a given event and can be collapsed
     * into one event. For example, an event can be seen as similar if all data
     * of the events but the timestamp is equal.
     *
     * @param otherEvent
     *            an event to compare
     * @return <code>true</code> if a given event is similar to another event
     *         else <code>false</code>
     */
    boolean isCollapsibleWith(ITmfEvent otherEvent);
}
