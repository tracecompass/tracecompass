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

package org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings;

/**
 * Interface for handling a color resource.
 *
 * @version 1.0
 * @author sveyrier
 */
public interface IColor {

    /**
     * Returns the contained color. Returned object must be an instance of org.eclipse.swt.graphics.Color if used with
     * the org.eclipse.linuxtools.tmf.ui.views.uml2sd.NGC graphical context
     *
     * @return the color
     */
    Object getColor();

    /**
     * Disposes the color
     */
    void dispose();

}
