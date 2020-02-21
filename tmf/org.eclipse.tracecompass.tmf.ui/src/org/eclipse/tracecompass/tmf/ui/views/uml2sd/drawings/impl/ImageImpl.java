/**********************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation, Ericsson
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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.drawings.IImage;

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

    /**
     * The image reference
     */
    private final Image fImage;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default constructor.
     *
     * @param file A file name of image file.
     */
    public ImageImpl(String file) {
        fImage = createResourceImage(file);
    }

    /**
     * Copy constructor
     *
     * @param image THe image to copy
     */
    public ImageImpl(Image image) {
        fImage = image;
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------
    /**
     * Returns Image object from file name.
     *
     * @param name File name of image file
     * @return image object or <code>null</code>
     */
    public Image getResourceImage(String name) {
        return createResourceImage(name);
    }

    @Override
    public Object getImage() {
        return fImage;
    }

    @Override
    public void dispose() {
        if (fImage != null) {
            fImage.dispose();
        }
    }

    /**
     * Returns Image object from file name.
     *
     * @param name File name of image file
     * @return image object or <code>null</code>
     */
    private static Image createResourceImage(String name) {
        try {
            URL BASIC_URL = new URL("platform", "localhost", "plugin");//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            URL url = new URL(BASIC_URL, "plugin/org.eclipse.tracecompass.tmf.ui/icons/" + name);//$NON-NLS-1$
            ImageDescriptor img = ImageDescriptor.createFromURL(url);
            return img.createImage();
        } catch (MalformedURLException e) {
            Activator.getDefault().logError("Error opening image file", e);  //$NON-NLS-1$
        }
        return null;
    }

}
