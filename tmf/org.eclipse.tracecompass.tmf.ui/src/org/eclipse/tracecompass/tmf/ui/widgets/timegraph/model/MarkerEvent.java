/*******************************************************************************
 * Copyright (c) 2015, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model;

import org.eclipse.swt.graphics.RGBA;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.IAnnotation;

/**
 * TimeEvent implementation for marker events
 *
 * @since 2.0
 */
public class MarkerEvent extends TimeEvent implements IMarkerEvent {

    private final String fCategory;
    private final RGBA fColor;
    private final String fLabel;
    private final boolean fForeground;

    /**
     * Standard constructor
     *
     * @param entry
     *            The entry of the marker, or null
     * @param time
     *            The timestamp of this marker
     * @param duration
     *            The duration of the marker
     * @param category
     *            The category of the marker
     * @param color
     *            The marker color
     * @param label
     *            The label of the marker, or null
     * @param foreground
     *            true if the marker is drawn in foreground, and false otherwise
     */
    public MarkerEvent(ITimeGraphEntry entry, long time, long duration, String category, RGBA color, String label, boolean foreground) {
        super(entry, time, duration);
        fCategory = category;
        fColor = color;
        fLabel = label;
        fForeground = foreground;
    }

    /**
     * Constructor
     *
     * @param entry
     *            The entry of the marker, or null
     * @param time
     *            The timestamp of this marker
     * @param duration
     *            The duration of the marker
     * @param category
     *            The category of the marker
     * @param color
     *            The marker color
     * @param label
     *            The label of the marker, or null
     * @param foreground
     *            true if the marker is drawn in foreground, and false otherwise
     * @param value
     *            The value of the marker
     */
    public MarkerEvent(ITimeGraphEntry entry, long time, long duration, String category, RGBA color, String label, boolean foreground, int value) {
        super(entry, time, duration, value);
        fCategory = category;
        fColor = color;
        fLabel = label;
        fForeground = foreground;
    }

    /**
     * Constructor
     *
     * @param annotation
     *            {@link IAnnotation} that represent this marker event
     * @param entry
     *            The entry to which this marker event is assigned
     * @param category
     *            The category of the marker
     * @param foreground
     *            true if the marker is drawn in foreground, and false otherwise
     * @since 5.2
     */
    public MarkerEvent(IAnnotation annotation, ITimeGraphEntry entry, String category, boolean foreground) {
        super(entry, annotation);
        fCategory = category;
        fLabel = annotation.getLabel();
        fColor = new RGBA(0, 0, 0, 255);
        fForeground = foreground;
    }

    @Override
    public String getCategory() {
        return fCategory;
    }

    @Override
    public RGBA getColor() {
        return fColor;
    }

    @Override
    public String getLabel() {
        return fLabel;
    }

    @Override
    public boolean isForeground() {
        return fForeground;
    }
}
