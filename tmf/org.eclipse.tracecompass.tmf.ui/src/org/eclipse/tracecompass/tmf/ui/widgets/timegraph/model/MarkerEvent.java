/*******************************************************************************
 * Copyright (c) 2015, 2019 Ericsson
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

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model;

import java.util.Objects;

import org.eclipse.swt.graphics.RGBA;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.IAnnotation;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.ui.model.StyleManager;

/**
 * TimeEvent implementation for marker events
 *
 * @since 2.0
 */
public class MarkerEvent extends TimeEvent implements IMarkerEvent {

    private static final RGBAColor BLACK = new RGBAColor(0,0,0);
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
        OutputElementStyle style = annotation.getStyle();
        RGBAColor color = null;
        if (style != null) {
            StyleManager styleManager = StyleManager.empty();
            color = styleManager.getColorStyle(style, StyleProperties.COLOR);
        }
        if (color == null) {
            color = BLACK;
        }
        fColor = new RGBA(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(getCategory(), getColor(), isForeground(), getLabel());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MarkerEvent other = (MarkerEvent) obj;
        return Objects.equals(getCategory(), other.getCategory())
                && Objects.equals(getColor(), other.getColor())
                && isForeground() == other.isForeground()
                && Objects.equals(getLabel(), other.getLabel());
    }
}
