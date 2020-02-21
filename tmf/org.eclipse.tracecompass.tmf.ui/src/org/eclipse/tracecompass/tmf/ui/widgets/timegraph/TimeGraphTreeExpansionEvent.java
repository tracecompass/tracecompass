/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph;

import java.util.EventObject;

import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;

/**
 * Notifier for the time graph view that a tree has been expanded.
 *
 * @author Patrick Tasse
 */
public class TimeGraphTreeExpansionEvent extends EventObject {

    /**
     * Default serial version UID for this class.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The entry that was expanded or collapsed.
     */
    private final ITimeGraphEntry fEntry;

    /**
     * Creates a new event for the given source and entry.
     *
     * @param source the tree viewer
     * @param entry the entry
     */
    public TimeGraphTreeExpansionEvent(Object source, ITimeGraphEntry entry) {
        super(source);
        fEntry = entry;
    }

    /**
     * Returns the entry that got expanded or collapsed.
     *
     * @return the entry
     */
    public ITimeGraphEntry getEntry() {
        return fEntry;
    }
}
