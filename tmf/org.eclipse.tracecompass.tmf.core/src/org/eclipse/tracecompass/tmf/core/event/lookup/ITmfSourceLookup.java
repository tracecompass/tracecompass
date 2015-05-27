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
}
