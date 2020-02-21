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
 * Notifier for the time graph that an object in the views has been selected.
 *
 * @author Patrick Tasse
 */
public class TimeGraphSelectionEvent extends EventObject {

    /**
     * Default serial version UID for this class.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The selected entry.
     */
    private final ITimeGraphEntry fSelection;

    /**
     * Standard constructor
     *
     * @param source
     *            The source of this event
     * @param selection
     *            The entry that was selected
     */
    public TimeGraphSelectionEvent(Object source, ITimeGraphEntry selection) {
        super(source);
        fSelection = selection;
    }

    /**
     * @return the selected entry or null if the selection is empty.
     */
    public ITimeGraphEntry getSelection() {
        return fSelection;
    }

}
