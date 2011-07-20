/**********************************************************************
 * Copyright (c) 2005, 2008, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ColorImpl.java,v 1.3 2008/01/24 02:28:50 apnan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.impl;

import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IColor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * @author sveyrier
 * 
 */
public class ColorImpl implements IColor {

    protected Color col = null;
    protected boolean manageColor = true;

    public ColorImpl(Display display, int r, int g, int b) {
        col = new Color(display, r, g, b);
    }

    protected ColorImpl(Color color) {
        col = color;
        manageColor = false;
    }

    public static ColorImpl getSystemColor(int color) {
        return new ColorImpl(Display.getDefault().getSystemColor(color));
    }

    @Override
    public Object getColor() {
        return col;
    }

    @Override
    public void dispose() {
        if ((col != null) && (manageColor))
            col.dispose();
    }

}
