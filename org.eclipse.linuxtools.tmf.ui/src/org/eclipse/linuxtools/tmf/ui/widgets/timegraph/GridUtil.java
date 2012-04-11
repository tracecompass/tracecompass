/**********************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: GridUtil.java,v 1.3 2006/09/20 19:49:13 ewchan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;

/**
 * A utility class to create convenient grid data objects.
 */ 
public class GridUtil
{
	/**
	 * Creates a grid data object that occupies vertical and horizontal
	 * space.
	 */
	static public GridData createFill() 
	{
		return new GridData(SWT.FILL, SWT.FILL, true, true);
	}
	/**
	 * Creates a grid data object that occupies horizontal space.
	 */
	static public GridData createHorizontalFill() 
	{		
		return new GridData(SWT.FILL, SWT.DEFAULT, true, false);		
	}
	/**
	 * Creates a grid data object that occupies vertical space.
	 */
	static public GridData createVerticalFill() 
	{
		return new GridData(SWT.DEFAULT, SWT.FILL, false, true);
	}
}
