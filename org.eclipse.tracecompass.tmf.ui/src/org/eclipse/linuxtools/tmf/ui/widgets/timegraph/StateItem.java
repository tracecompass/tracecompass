/**********************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.widgets.timegraph;

import org.eclipse.swt.graphics.RGB;

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

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     *  The State color
     */
    private RGB fStateColor;
    /**
     * The State string.
     */
    private String fStateString;


    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Creates a state item with given color and unspecified name.
     *
     * @param stateColor A state color
     */
    public StateItem(RGB stateColor) {
        this(stateColor, UNDEFINED_STATE_NAME);
    }

    /**
     * Creates a state color - state string pair.
     *
     * @param stateColor A state color
     * @param stateString A state string
     */
    public StateItem(RGB stateColor, String stateString) {
        fStateColor = stateColor;
        fStateString = stateString;
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
        return fStateColor;
    }

    /**
     * Sets the state color.
     *
     * @param stateColor A state color to set
     */
    public void setStateColor(RGB stateColor) {
        fStateColor = stateColor;
    }

    /**
     * Returns the state string.
     *
     * @return the state string.
     */
    public String getStateString() {
        return fStateString;
    }

    /**
     * Sets the state string
     * @param stateString A state string to set
     */
    public void setStateString(String stateString) {
        fStateString = stateString;
    }
}
