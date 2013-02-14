/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Updated for location in checkpoint
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace;

import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;

/**
 * The basic trace checkpoint structure in TMF. The purpose of the checkpoint is
 * to associate a trace location to an event timestamp.
 *
 * @version 1.0
 * @author Francois Chouinard
 *
 * @see ITmfTimestamp
 * @see ITmfLocation
 */
public interface ITmfCheckpoint extends Comparable<ITmfCheckpoint> {

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * @return the timestamp of the event referred to by the context
     * @since 2.0
     */
    public ITmfTimestamp getTimestamp();

    /**
     * @return the location of the event referred to by the checkpoint
     */
    public ITmfLocation getLocation();

    // ------------------------------------------------------------------------
    // Comparable
    // ------------------------------------------------------------------------

    @Override
    public int compareTo(ITmfCheckpoint checkpoint);

}
