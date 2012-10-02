/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph;

import java.util.EventObject;

import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;

/**
 * Notifier for the time graph that an object in the views has been selected.
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TimeGraphSelectionEvent extends EventObject {

    /**
     * Default serial version UID for this class.
     * @since 1.0
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
