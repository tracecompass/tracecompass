/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.core.event.collapse;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;

/**
 * Interface for deciding whether an event can be collapsed with another event.
 *
 * @author Bernd Hufmann
 * @since 3.2
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
