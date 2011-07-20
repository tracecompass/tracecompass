/**********************************************************************
 * Copyright (c) 2005, 2006, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: LifelineCategories.java,v 1.2 2006/09/20 20:56:25 ewchan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.core;

import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IImage;

/**
 * LifelineCategories is used to assign additional description for 
 * lifelines of the same type. This consists in providing a type name and an icon.
 * The icon will be displayed in the rectangle which contains the lifeline name.
 * The category name is only display in the lifeline tooltip.
 */
public class LifelineCategories {
	
	/**
	 * The category name
	 */
	protected String name = null;
	/**
	 * The category image
	 */
	protected IImage categoryImage = null;
	
	/**
	 * Returns the category name
	 * @return the category name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the category name
	 * @param string the name
	 */
	public void setName(String string) {
		name = string;
	}
	   
	/**
	 * Returns the category icon
	 * @return the category icon
	 */
	public IImage getImage()
	{
		return categoryImage;
	}
	
	/**
	 * Set the category icon
	 * @param image the icon
	 */
	public void setImage(IImage image)
	{
		categoryImage = image;
	}
}
