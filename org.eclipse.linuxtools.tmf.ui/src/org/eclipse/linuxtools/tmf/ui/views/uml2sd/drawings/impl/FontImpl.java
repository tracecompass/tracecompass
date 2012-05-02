/**********************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * Copyright (c) 2011, 2012 Ericsson.
 * 
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.impl;

import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IFont;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

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
    protected Font font = null;
    /**
     * Flag to indicate that this object is managing the resource.
     */
    protected boolean manageFont = true;

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
        font = new Font(display, data);
    }

    /**
     * Copy constructor
     * 
     * @param value A font to copy.
     */
    protected FontImpl(Font value) {
        font = value;
        manageFont = false;
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

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IFont#getFont()
     */
    @Override
    public Object getFont() {
        return font;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IFont#dispose()
     */
    @Override
    public void dispose() {
        if (font != null) {
            font.dispose();
        }
    }
}
