/**********************************************************************
 * Copyright (c) 2005, 2006, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ISDGraphNodeSupporter.java,v 1.2 2006/09/20 20:56:26 ewchan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider;

/**
 * Sequence Diagram loaders which implement this class provide the actions for finding or filtering the sequence
 * diagram. This interface also allow the implementor to set which action/feature are supported
 * 
 * Action provider are associated to a Sequence Diagram SDWidget calling SDViewer.setSDFindProvider() or
 * SDViewer.setSDFilterProvider
 * 
 * @author sveyrier
 * 
 */
public abstract interface ISDGraphNodeSupporter {

    public static int LIFELINE = 0;
    public static int SYNCMESSAGE = 1;
    public static int SYNCMESSAGERETURN = 2;
    public static int ASYNCMESSAGE = 3;
    public static int ASYNCMESSAGERETURN = 4;
    public static int STOP = 5;

    /**
     * Return true to enable this options, false otherwise
     * 
     * @return true to enable this options, false otherwise
     */
    public boolean isNodeSupported(int nodeType);

    /**
     * Return the name to use in dialogs Not called if isNodeSupported return false
     * 
     * @return the name to use in dialogs
     */
    public String getNodeName(int nodeType, String loaderClassName);
}
