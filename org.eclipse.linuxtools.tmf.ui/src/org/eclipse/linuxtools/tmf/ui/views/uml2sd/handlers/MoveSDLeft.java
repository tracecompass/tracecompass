/**********************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation, Ericsson
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Bernd Hufmann - Updated for TMF
 **********************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers;

import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDView;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDWidget;

/**
 * Action class implementation to move left in the sequence diagram view within a page.
 *
 * @version 1.0
 * @author sveyrier
 *
 */
public class MoveSDLeft extends BaseSDAction {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The action ID.
     */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.MoveSDLeft"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default constructor
     */
    public MoveSDLeft(){
        this(null);
    }

    /**
     * Constructor
     *
     * @param view a sequence diagram view reference
     */
    public MoveSDLeft(SDView view) {
        super(view);
        setId(ID);
        setActionDefinitionId(ID);
    }

   // ------------------------------------------------------------------------
   // Operations
   // ------------------------------------------------------------------------

    @Override
    public void run() {

        if (getView() == null) {
            return;
        }

        SDWidget viewer = getView().getSDWidget();
        if (viewer != null) {
            viewer.scrollBy(-viewer.getVisibleWidth(), 0);
        }
    }
}
