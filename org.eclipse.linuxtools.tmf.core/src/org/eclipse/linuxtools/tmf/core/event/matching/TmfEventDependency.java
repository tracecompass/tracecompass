/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.event.matching;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;

/**
 * Represents a dependency (match) between two events, where source event leads
 * to destination event
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class TmfEventDependency {

    private final ITmfEvent fSourceEvent;
    private final ITmfEvent fDestEvent;

    /**
     * Constructor
     *
     * @param source
     *            The source event of this dependency
     * @param destination
     *            The destination event of this dependency
     */
    public TmfEventDependency(final ITmfEvent source, final ITmfEvent destination) {
        fSourceEvent = source;
        fDestEvent = destination;
    }

    /**
     * Getter for fSourceEvent
     *
     * @return The source event
     */
    public ITmfEvent getSourceEvent() {
        return fSourceEvent;
    }

    /**
     * Getter for fDestEvent
     *
     * @return the Destination event
     */
    public ITmfEvent getDestinationEvent() {
        return fDestEvent;
    }

}
