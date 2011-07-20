/**********************************************************************
 * Copyright (c) 2005, 2006, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: MoveSDLeft.java,v 1.2 2006/09/20 20:56:26 ewchan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers;

import org.eclipse.jface.action.Action;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDView;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDWidget;

/**
 * @author sveyrier
 * 
 */
public class MoveSDLeft extends Action {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    public final static String ID = "org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.MoveSDLeft"; //$NON-NLS-1$

    protected SDView fView = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    public MoveSDLeft(){
        this(null);
    }

    public MoveSDLeft(SDView view) {
        super();
        setId(ID);
        setActionDefinitionId(ID);
        fView = view;
    }

   // ------------------------------------------------------------------------
   // Operations
   // ------------------------------------------------------------------------
   /*
    * (non-Javadoc)
    * @see org.eclipse.jface.action.Action#run()
    */    @Override
    public void run() {
        
        if (fView == null) {
            return;
        }
        
        SDWidget viewer = fView.getSDWidget();
        if (viewer != null) {
            viewer.scrollBy(-viewer.getVisibleWidth(), 0);
        }
    }
    
    /**
     * Sets the active SD view.
     * @param view The SD view.
     */
   public void setView(SDView view) {
        fView = view;
    }
}
