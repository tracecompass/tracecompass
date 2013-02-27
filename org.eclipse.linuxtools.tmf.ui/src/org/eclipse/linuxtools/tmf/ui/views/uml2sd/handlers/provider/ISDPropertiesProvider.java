/**********************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation, Ericsson
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Bernd Hufmann - Updated for TMF
 **********************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider;

import org.eclipse.ui.views.properties.IPropertySheetPage;

/**
 * Interface for providing sequence diagram property.
 *
 * Contract for loaders that want to provide information in the properties view.
 *
 * @version 1.0
 * @author sveyrier

 */
public interface ISDPropertiesProvider {

    /**
     * Returns the IPropertySheetEntry that will fill in the properties view
     *
     * @return the property sheet entry
     */
    public IPropertySheetPage getPropertySheetEntry();

}
