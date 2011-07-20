/**********************************************************************
 * Copyright (c) 2005, 2008, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: FontImpl.java,v 1.3 2008/01/24 02:28:50 apnan Exp $
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
 * @author sveyrier
 * 
 */
public class FontImpl implements IFont {

    protected Font font = null;
    protected boolean manageFont = true;

    public FontImpl(Display display, FontData data) {
        font = new Font(display, data);
    }

    protected FontImpl(Font value) {
        font = value;
        manageFont = false;
    }

    @Override
    public Object getFont() {
        return font;
    }

    public static FontImpl getSystemFont() {
        return new FontImpl(Display.getDefault().getSystemFont());
    }

    @Override
    public void dispose() {
        if (font != null)
            font.dispose();
    }

}
