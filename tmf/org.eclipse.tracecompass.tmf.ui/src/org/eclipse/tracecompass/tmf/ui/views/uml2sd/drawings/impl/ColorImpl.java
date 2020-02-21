/**********************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation, Ericsson
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Bernd Hufmann - Updated for TMF
 **********************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.uml2sd.drawings.impl;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.drawings.IColor;

/**
 * Default implementation of the IColor interface.
 *
 * @version 1.0
 * @author sveyrier
 *
 */
public class ColorImpl implements IColor {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The color object.
     */
    private final Color fColor;
    /**
     * Flag to indicate that this object is managing the resource.
     */
    private boolean fManageColor = true;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param display The display to use
     * @param r A value for red
     * @param g A value for green
     * @param b A value for blue
     */
    public ColorImpl(Display display, int r, int g, int b) {
        fColor = new Color(display, r, g, b);
    }

    /**
     * Constructor
     *
     * @param display The display to use
     * @param color the RGB color
     */
    public ColorImpl(Display display, RGB color) {
        fColor = new Color(display, color.red, color.green, color.blue);
    }

    /**
     * Copy constructor
     *
     * @param color
     *            A color to copy
     */
    protected ColorImpl(Color color) {
        fColor = color;
        fManageColor = false;
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /**
     * Returns a system color.
     *
     * @param color The color ID
     * @return a system color
     */
    public static ColorImpl getSystemColor(int color) {
        return new ColorImpl(Display.getDefault().getSystemColor(color));
    }

    @Override
    public Object getColor() {
        return fColor;
    }

    @Override
    public void dispose() {
        if ((fColor != null) && (fManageColor)) {
            fColor.dispose();
        }
    }

}
