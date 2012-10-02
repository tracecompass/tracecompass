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

/**
 * Event for the time graph view
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TimeGraphTimeEvent extends EventObject {

    /**
     * Default serial version UID for this class.
     * @since 1.0
     */
    private static final long serialVersionUID = 1L;

    /**
     * The selected time.
     */
    private final long fTime;

    /**
     * Standard constructor
     *
     * @param source
     *            The source of this event
     * @param time
     *            The time that was requested
     */
    public TimeGraphTimeEvent(Object source, long time) {
        super(source);
        fTime = time;
    }

    /**
     * @return the selected time
     */
    public long getTime() {
        return fTime;
    }

}
