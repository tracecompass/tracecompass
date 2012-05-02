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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.linuxtools.internal.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IImage;
import org.eclipse.swt.graphics.Image;

/**
 * Default implementation of the IImage interface.
 * 
 * @version 1.0
 * @author sveyrier
 * 
 */
public class ImageImpl implements IImage {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
   
    protected Image img = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default constructor.
     * 
     * @param file A file name of image file.
     */
    public ImageImpl(String file) {
        img = getResourceImage(file);
    }

    /**
     * Copy constructor
     * 
     * @param img_ THe image to copy
     */
    public ImageImpl(Image img_) {
        img = img_;
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------
    /**
     * Returns Image object from file name.
     * 
     * @param _name File name of image file
     * @return image object or <code>null</code>
     */
    public Image getResourceImage(String _name) {
        try {
            URL BASIC_URL = new URL("platform", "localhost", "plugin");//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            URL url = new URL(BASIC_URL, "plugin/org.eclipse.linuxtools.tmf.ui/icons/" + _name);//$NON-NLS-1$
            ImageDescriptor img = ImageDescriptor.createFromURL(url);
            return img.createImage();
        } catch (MalformedURLException e) {
            TmfUiPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, TmfUiPlugin.PLUGIN_ID, "Error opening image file", e));  //$NON-NLS-1$
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IImage#getImage()
     */
    @Override
    public Object getImage() {
        return img;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IImage#dispose()
     */
    @Override
    public void dispose() {
        if (img != null) {
            img.dispose();
        }
    }
}
