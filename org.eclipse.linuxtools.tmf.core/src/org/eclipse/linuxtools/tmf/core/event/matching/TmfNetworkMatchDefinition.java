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
import org.eclipse.linuxtools.tmf.core.event.matching.TmfNetworkEventMatching.Direction;

/**
 * Base class for all network match definitions, ie traces with send and receive
 * events
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public abstract class TmfNetworkMatchDefinition implements ITmfMatchEventDefinition {

    /**
     * Returns the direction of this event, whether 'send', 'receive' or null if
     * event is neither
     *
     * @param event
     *            The event to check
     * @return The direction of this event, null if uninteresting event
     */
    public abstract Direction getDirection(ITmfEvent event);

}
