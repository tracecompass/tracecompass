/**********************************************************************
 * Copyright (c) 2005, 2006, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ISDCollapseProvider.java,v 1.2 2006/09/20 20:56:26 ewchan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider;

import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Lifeline;

/**
 * Sequence diagram loaders which want to support Drag and Drop collapsing in the sequence diagram must implement this
 * interface and register this implementation using SDViewer.setCollapsingProvider();
 * 
 * @author sveyrier
 */
public interface ISDCollapseProvider {

    /**
     * Called back when the sequence diagram is requesting 2 lifelines collapsing
     * 
     * @param lifeline1 - One of the lifeline to collapse
     * @param lifeline2 - The other lifeline to collapse with
     */
    public void collapseTwoLifelines(Lifeline lifeline1, Lifeline lifeline2);

}
