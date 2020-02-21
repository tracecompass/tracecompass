/**********************************************************************
 * Copyright (c) 2012, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.tmf.ui.widgets.timegraph;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.tracecompass.internal.tmf.ui.util.StylePropertiesUtils;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;
import org.eclipse.tracecompass.tmf.ui.colors.ColorUtils;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEventStyleStrings;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphControl;

import com.google.common.collect.ImmutableMap;

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

    private static final String UNDEFINED_COLOR_VALUE = "#000000"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final Map<String, Object> fOriginalStyleMap;
    private final Map<String, Object> fStyleMap;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Creates a state item with given color and unspecified name.
     *
     * @param stateColor
     *            A state color
     * @deprecated use {@link StateItem#StateItem(Map)}
     */
    @Deprecated
    public StateItem(RGB stateColor) {
        this(stateColor, stateColor.toString());
    }

    /**
     * Copy constructor, from a map
     *
     * @param style
     *            the map of styles
     * @since 3.2
     */
    public StateItem(Map<String, Object> style) {
        Map<String, Object> styleMap = new HashMap<>();
        styleMap.putAll(style);
        fStyleMap = StylePropertiesUtils.updateEventStyleProperties(styleMap);
        fOriginalStyleMap = ImmutableMap.copyOf(fStyleMap);
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
        Map<String, Object> styleMap = new HashMap<>();
        String hexColor = ColorUtils.toHexColor(stateColor.red, stateColor.green, stateColor.blue);
        styleMap.put(StyleProperties.BACKGROUND_COLOR, hexColor);
        styleMap.put(StyleProperties.COLOR, hexColor);
        styleMap.put(StyleProperties.STYLE_NAME, stateString);
        fStyleMap = styleMap;
        fOriginalStyleMap = ImmutableMap.copyOf(styleMap);
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
        String rgb = (String) fStyleMap.getOrDefault(StyleProperties.BACKGROUND_COLOR,
                fStyleMap.getOrDefault(StyleProperties.COLOR, UNDEFINED_COLOR_VALUE));
        return ColorUtils.fromHexColor(rgb);
    }

    /**
     * Sets the state color.
     *
     * @param stateColor
     *            A state color to set
     */
    public void setStateColor(RGB stateColor) {
        if (stateColor != null) {
            String hexColor = ColorUtils.toHexColor(stateColor.red, stateColor.green, stateColor.blue);
            fStyleMap.put(StyleProperties.BACKGROUND_COLOR, hexColor);
            fStyleMap.put(StyleProperties.COLOR, hexColor);
        }
    }

    /**
     * Returns the state height factor.
     *
     * @return Returns the state height factor.
     * @since 4.3
     */
    public float getStateHeightFactor() {
        Object itemType = fStyleMap.get(ITimeEventStyleStrings.itemTypeProperty());
        float defaultStateWidth = ITimeEventStyleStrings.linkType().equals(itemType) ?
                TimeGraphControl.DEFAULT_LINK_WIDTH : TimeGraphControl.DEFAULT_STATE_WIDTH;
        return (float) fStyleMap.getOrDefault(StyleProperties.HEIGHT, defaultStateWidth);
    }

    /**
     * Returns the state string.
     *
     * @return the state string.
     */
    public String getStateString() {
        return String.valueOf(fStyleMap.getOrDefault(StyleProperties.STYLE_NAME, UNDEFINED_STATE_NAME));
    }

    /**
     * Reset the style to the original values
     *
     * @since 3.2
     */
    public void reset() {
        fStyleMap.clear();
        fStyleMap.putAll(fOriginalStyleMap);
    }

    /**
     * Sets the state string
     *
     * @param stateString
     *            A state string to set
     */
    public void setStateString(String stateString) {
        fStyleMap.put(StyleProperties.STYLE_NAME, stateString);
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
        return fStyleMap;
    }
}
