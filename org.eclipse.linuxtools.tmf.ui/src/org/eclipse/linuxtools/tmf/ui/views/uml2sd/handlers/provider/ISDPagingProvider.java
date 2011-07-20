/**********************************************************************
 * Copyright (c) 2005, 2006, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ISDPagingProvider.java,v 1.2 2006/09/20 20:56:26 ewchan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider;

/**
 * Sequence Diagram loaders which implement this class provide the actions for sequence diagram page navigation.
 * 
 * Action provider are associated to a Sequence Diagram SDWidget calling SDViewer.setSDPagingProvider()
 * 
 * @author sveyrier
 * 
 */
public interface ISDPagingProvider {

    /**
     * Return true to enable the next page button in the coolBar, false otherwise
     * 
     * @return true if a next page exists false otherwise
     */
    public boolean hasNextPage();

    /**
     * Return true to enable the previous page button in the coolBar, false otherwise
     * 
     * @return true if a previous page exists false otherwise
     */
    public boolean hasPrevPage();

    /**
     * Called back when next page button is pressed in the coolBar
     */
    public void nextPage();

    /**
     * Called back when previous page button is pressed in the coolBar
     */
    public void prevPage();
    
    /**
     * Called back when first page button is pressed in the coolBar
     */
    public void firstPage();
    
    /**
     * Called back when last page button is pressed in the coolBar
     */
    public void lastPage();
}
