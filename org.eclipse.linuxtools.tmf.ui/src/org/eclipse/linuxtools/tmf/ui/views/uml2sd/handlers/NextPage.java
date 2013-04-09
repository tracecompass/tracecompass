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

import org.eclipse.jface.action.Action;
import org.eclipse.linuxtools.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDView;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.SDMessages;

/**
 * Action class implementation to move the focus to the next page of the whole sequence diagram.
 *
 * @version 1.0
 * @author sveyrier
 *
 */
public class NextPage extends Action {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The action ID.
     */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.nextpage"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The sequence diagram view reference.
     */
    protected SDView fView = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default constructor
     *
     * @param view the view reference
     */
    public NextPage(SDView view) {
        super();
        fView = view;
        setText(SDMessages._36);
        setToolTipText(SDMessages._38);
        setId(ID);
        setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_NEXT_PAGE));
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

     @Override
    public void run() {
        if ((fView == null) || (fView.getSDWidget()) == null) {
            return;
        }
        if (fView.getSDPagingProvider() != null) {
            fView.getSDPagingProvider().nextPage();
        }
        fView.updateCoolBar();
        fView.getSDWidget().redraw();
    }
}
