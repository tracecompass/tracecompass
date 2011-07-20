/**********************************************************************
 * Copyright (c) 2005, 2006, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ISDExtendedActionBarProvider.java,v 1.2 2006/09/20 20:56:26 ewchan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider;

import org.eclipse.ui.IActionBars;

/**
 * Sequence Diagram loaders which implement this interface provide their own action to the action bar.
 * 
 * Action provider are associated to a Sequence Diagram SDWidget calling SDViewer.setExtendedActionBarProvider()
 * 
 */
public interface ISDExtendedActionBarProvider {

    /**
     * The caller is supposed to add its own actions in the cool bar and the drop-down menu.<br>
     * See examples in SDView.createCoolbarContent().
     * 
     * @param bar the bar
     */
    void supplementCoolbarContent(IActionBars bar);

}
