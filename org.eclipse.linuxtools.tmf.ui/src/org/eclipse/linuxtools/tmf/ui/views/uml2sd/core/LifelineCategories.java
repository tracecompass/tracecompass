/**********************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.core;

import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IImage;

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
	protected String name = null;
	/**
	 * The category image
	 */
	protected IImage categoryImage = null;

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

	/**
	 * Returns the category name.
	 * 
	 * @return the category name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the category name.
	 * 
	 * @param string the name
	 */
	public void setName(String string) {
		name = string;
	}
	   
	/**
	 * Returns the category icon.
	 * 
	 * @return the category icon
	 */
	public IImage getImage() {
		return categoryImage;
	}
	
	/**
	 * Sets the category icon.
	 * 
	 * @param image the icon
	 */
	public void setImage(IImage image) {
		categoryImage = image;
	}
}
