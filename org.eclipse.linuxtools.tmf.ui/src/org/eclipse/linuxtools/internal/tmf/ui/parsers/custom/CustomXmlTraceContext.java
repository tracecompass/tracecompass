/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.parsers.custom;

import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.location.ITmfLocation;

/**
 * Trace context for custom XML traces.
 *
 * @author Patrick Tassé
 */
public class CustomXmlTraceContext extends TmfContext {

    /**
     * Constructor
     *
     * @param location
     *            The location (in the file) of this context
     * @param rank
     *            The rank of the event pointed by this context
     */
    public CustomXmlTraceContext(ITmfLocation location, long rank) {
        super(location, rank);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof CustomXmlTraceContext)) {
            return false;
        }
        return true;
    }

}