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
 * Action class implementation to move the focus to the previous page of the whole sequence diagram.
 *
 * @version 1.0
 * @author sveyrier
 *
 */
public class PrevPage extends Action {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The action ID.
     */
    public final static String ID = "org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.prevpage"; //$NON-NLS-1$

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
    public PrevPage(SDView view) {
        super();
        fView = view;
        setText(SDMessages._35);
        setToolTipText(SDMessages._37);
        setId(ID);
        setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_PREV_PAGE));
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    @Override
    public void run() {
        if ((fView == null) || (fView.getSDWidget()) == null) {
            return;
        }
        if (fView.getSDPagingProvider() != null) {
            fView.getSDPagingProvider().prevPage();
        }
        fView.updateCoolBar();
        fView.getSDWidget().redraw();
    }
}
