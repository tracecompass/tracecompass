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
package org.eclipse.tracecompass.tmf.ui.views.uml2sd.core;

import org.eclipse.tracecompass.tmf.ui.views.uml2sd.drawings.IImage;

/**
 * <p>
 * LifelineCategories is used to assign additional description for
 * lifelines of the same type. This consists in providing a type name and an icon.
 * The icon will be displayed in the rectangle which contains the lifeline name.
 * The category name is only display in the lifeline tooltip.
 * </p>
 *
 * @version 1.0
 * @author sveyrier
 */
public class LifelineCategories {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The category name
     */
    private String fName = null;
    /**
     * The category image
     */
    private IImage fCategoryImage = null;

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /**
     * Returns the category name.
     *
     * @return the category name
     */
    public String getName() {
        return fName;
    }

    /**
     * Sets the category name.
     *
     * @param string the name
     */
    public void setName(String string) {
        fName = string;
    }

    /**
     * Returns the category icon.
     *
     * @return the category icon
     */
    public IImage getImage() {
        return fCategoryImage;
    }

    /**
     * Sets the category icon.
     *
     * @param image the icon
     */
    public void setImage(IImage image) {
        fCategoryImage = image;
    }
}
