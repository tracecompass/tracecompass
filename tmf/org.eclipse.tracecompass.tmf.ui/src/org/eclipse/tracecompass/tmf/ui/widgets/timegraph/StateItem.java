/**********************************************************************
 * Copyright (c) 2012, 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.tmf.ui.widgets.timegraph;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEventStyleStrings;

/**
 * Class that contains the color of a state and the corresponding state string
 * to display.
 *
 * @version 1.0
 * @author Bernd Hufmann
 */
public class StateItem {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * Name of state if not known
     */
    public static final String UNDEFINED_STATE_NAME = "Undefined"; //$NON-NLS-1$

    private static final int UNDEFINED_COLOR_VALUE = 0xff;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final Map<String, Object> fStyleMap;

    private final Map<String, Object> fReadOnlyStyleMap;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Creates a state item with given color and unspecified name.
     *
     * @param stateColor
     *            A state color
     */
    public StateItem(RGB stateColor) {
        this(stateColor, UNDEFINED_STATE_NAME);
    }

    /**
     * Creates a state color - state string pair.
     *
     * @param stateColor
     *            A state color
     * @param stateString
     *            A state string
     */
    public StateItem(RGB stateColor, String stateString) {
        int stateColorInt = stateColor.red << 24 | stateColor.green << 16 | stateColor.blue << 8 | 0xff;
        fStyleMap = new HashMap<>();
        fReadOnlyStyleMap = Collections.unmodifiableMap(fStyleMap);
        fStyleMap.put(ITimeEventStyleStrings.fillStyle(), ITimeEventStyleStrings.solidColorFillStyle());
        fStyleMap.put(ITimeEventStyleStrings.fillColor(), stateColorInt);
        fStyleMap.put(ITimeEventStyleStrings.label(), stateString);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * Returns the state color.
     *
     * @return Returns the state color.
     */
    public RGB getStateColor() {
        int rgbInt = (int) fStyleMap.getOrDefault(ITimeEventStyleStrings.fillColor(), UNDEFINED_COLOR_VALUE);
        return new RGB((rgbInt >> 24) & 0xff, (rgbInt >> 16) & 0xff, (rgbInt >> 8) & 0xff);
    }

    /**
     * Sets the state color.
     *
     * @param stateColor
     *            A state color to set
     */
    public void setStateColor(RGB stateColor) {
        fStyleMap.put(ITimeEventStyleStrings.fillColor(), stateColor);
    }

    /**
     * Returns the state string.
     *
     * @return the state string.
     */
    public String getStateString() {
        return String.valueOf(fStyleMap.getOrDefault(ITimeEventStyleStrings.label(), UNDEFINED_STATE_NAME));
    }

    /**
     * Sets the state string
     *
     * @param stateString
     *            A state string to set
     */
    public void setStateString(String stateString) {
        fStyleMap.put(ITimeEventStyleStrings.label(), stateString);
    }

    /**
     * Gets the height factor of a given state (how thick it will be when
     * displayed)
     *
     * @return The map of styles
     *
     * @since 3.0
     */
    public Map<String, Object> getStyleMap() {
        return fReadOnlyStyleMap;
    }
}
