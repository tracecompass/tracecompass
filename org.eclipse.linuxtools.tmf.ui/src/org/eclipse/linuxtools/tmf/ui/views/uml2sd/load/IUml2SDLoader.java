/**********************************************************************
 * Copyright (c) 2005, 2006, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: IUml2SDLoader.java,v 1.2 2006/09/20 20:56:28 ewchan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.load;

import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDView;

/**
 * The interface all UML2SD loaders must implement
 */
public interface IUml2SDLoader {

    /**
     * Set the viewer object to the loader that has been reloaded at the beginning 
     * of a new workbench session
     */
    public void setViewer(SDView viewer);

    /**
     * Title string for the UML2SD View when this loader is the one
     * 
     * @return the string convenient for this loader
     */
    public String getTitleString();

    /**
     * When another loader becomes the one the previous one is replaced It's time clean-up 
     * if needed (listeners to be removed for example)
     */
    public void dispose();

}
