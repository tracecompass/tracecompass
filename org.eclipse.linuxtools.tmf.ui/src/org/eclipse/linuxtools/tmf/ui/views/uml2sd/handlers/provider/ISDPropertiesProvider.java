/**********************************************************************
 * Copyright (c) 2005, 2006, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ISDPropertiesProvider.java,v 1.2 2006/09/20 20:56:26 ewchan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider;

import org.eclipse.ui.views.properties.IPropertySheetPage;

/**
 * Contract for loaders that want to provide information in the properties view
 */
public interface ISDPropertiesProvider {

    /**
     * Returns the IPropertySheetEntry that will fill in the properties view
     * 
     * @return the property sheet entry
     */
    public IPropertySheetPage getPropertySheetEntry();

}
