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

package org.eclipse.tracecompass.tmf.ui.views.uml2sd.drawings;

/**
 * Interface for handling a font resource.
 *
 * @version 1.0
 * @author sveyrier
 */
public interface IFont {

    /**
     * Returns the contained font. Returned object must be an instance of org.eclipse.swt.graphics.Font if used with the
     * org.eclipse.tracecompass.tmf.ui.views.uml2sd.NGC graphical context
     *
     * @return the font
     */
    Object getFont();

    /**
     * Disposes the font
     */
    void dispose();

}
