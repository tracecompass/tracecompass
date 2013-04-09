/**********************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation, Ericsson
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Bernd Hufmann - Updated for TMF
 **********************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.impl;

import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IColor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

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
    protected Color fColor = null;
    /**
     * Flag to indicate that this object is managing the resource.
     */
    protected boolean fManageColor = true;

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
