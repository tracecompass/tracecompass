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

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.drawings.IFont;

/**
 * Default implementation of the IFont interface.
 *
 * @version 1.0
 * @author sveyrier
 *
 */
public class FontImpl implements IFont {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The font object
     */
    private final Font fFont;
    /**
     * Flag to indicate that this object is managing the resource.
     */
    protected boolean fManageFont = true;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default constructor.
     *
     * @param display The display to use
     * @param data A font data
     */
    public FontImpl(Display display, FontData data) {
        fFont = new Font(display, data);
    }

    /**
     * Copy constructor
     *
     * @param value A font to copy.
     */
    protected FontImpl(Font value) {
        fFont = value;
        fManageFont = false;
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /**
     * Returns a font implementation based system font.
     *
     * @return a font implementation based system font.
     */
    public static FontImpl getSystemFont() {
        return new FontImpl(Display.getDefault().getSystemFont());
    }

    @Override
    public Object getFont() {
        return fFont;
    }

    @Override
    public void dispose() {
        if (fFont != null) {
            fFont.dispose();
        }
    }
}
