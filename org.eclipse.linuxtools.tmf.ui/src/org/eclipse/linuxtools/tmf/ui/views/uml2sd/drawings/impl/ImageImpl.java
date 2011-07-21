/**********************************************************************
 * Copyright (c) 2005, 2008, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ImageImpl.java,v 1.3 2008/01/24 02:28:50 apnan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.impl;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IImage;
import org.eclipse.swt.graphics.Image;

/**
 * @author sveyrier
 * 
 */
public class ImageImpl implements IImage {

    protected Image img = null;

    public ImageImpl(String file) {
        img = getResourceImage(file);
    }

    public ImageImpl(Image img_) {
        img = img_;
    }
    
    public Image getResourceImage(String _name) {
        try {
            URL BASIC_URL = new URL("platform", "localhost", "plugin");//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            URL url = new URL(BASIC_URL, "plugin/org.eclipse.linuxtools.tmf.ui/icons/" + _name);//$NON-NLS-1$
            ImageDescriptor img = ImageDescriptor.createFromURL(url);
            return img.createImage();
        } catch (MalformedURLException E) {
            System.err.println(E);
        }
        return null;
    }

    @Override
    public Object getImage() {
        return img;
    }

    @Override
    public void dispose() {
        if (img != null)
            img.dispose();
    }
}
