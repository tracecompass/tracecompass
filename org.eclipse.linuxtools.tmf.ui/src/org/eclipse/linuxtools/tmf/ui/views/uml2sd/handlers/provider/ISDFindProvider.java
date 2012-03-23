/**********************************************************************
 * Copyright (c) 2005, 2006, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ISDFindProvider.java,v 1.2 2006/09/20 20:56:26 ewchan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider;

import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.widgets.Criteria;

/**
 * Sequence Diagram loaders which implement this class provide the actions for finding the sequence diagram. This
 * interface also allow the implementor to set which action/feature are supported
 * 
 * Action provider are associated to a Sequence Diagram SDWidget calling SDViewer.setSDFindProvider()
 * 
 * @author sveyrier
 * 
 */
public interface ISDFindProvider extends ISDGraphNodeSupporter {

    /**
     * Called when the Find dialog box OK button is pressed
     * 
     * @param toApply user selection made in the dialog box
     * @return true if the find got a non empty result
     */
    public boolean find(Criteria toApply);

    /**
     * Called when dialog is closed
     */
    public void cancel();

}
