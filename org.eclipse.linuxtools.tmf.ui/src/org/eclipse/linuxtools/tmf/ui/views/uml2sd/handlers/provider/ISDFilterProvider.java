/**********************************************************************
 * Copyright (c) 2005, 2006, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ISDFilterProvider.java,v 1.2 2006/09/20 20:56:26 ewchan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider;

import java.util.List;

/**
 * Sequence Diagram loaders which implement this class provide the actions filtering the sequence diagram.<br>
 * This interface also allow the implementor to set which graph nodes are supporting filtering (thanks to
 * ISDGraphNodeSupporter extension).<br>
 * 
 * Action provider are associated to a Sequence Diagram SDWidget calling <code>SDViewer.setSDFilterProvider</code><br>
 * 
 * Filters to be applied to be managed by the loader in an ArrayList of FilterCriteria.<br>
 */
public interface ISDFilterProvider extends ISDGraphNodeSupporter {

    /**
     * Called when the Filter dialog box OK button is pressed
     * 
     * @param filters user selection made in the dialog box
     * @return true if the filter applied
     */
    public boolean filter(List<?> filters);

}
