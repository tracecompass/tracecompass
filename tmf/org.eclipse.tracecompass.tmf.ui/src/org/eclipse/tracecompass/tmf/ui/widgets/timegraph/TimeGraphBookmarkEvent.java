/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph;

import java.util.EventObject;

import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.IMarkerEvent;

/**
 * Bookmark event
 *
 * @author Patrick Tasse
 * @since 2.0
 */
public class TimeGraphBookmarkEvent extends EventObject {

    /**
     * Default serial version UID for this class.
     */
    private static final long serialVersionUID = 8155869849072620814L;

    /**
     * The bookmark
     */
    private final IMarkerEvent fBookmark;

    /**
     * Standard constructor
     *
     * @param source
     *            The source of this event
     * @param bookmark
     *            The bookmark
     */
    public TimeGraphBookmarkEvent(Object source, IMarkerEvent bookmark) {
        super(source);
        fBookmark = bookmark;
    }

    /**
     * @return the bookmark
     */
    public IMarkerEvent getBookmark() {
        return fBookmark;
    }

}
