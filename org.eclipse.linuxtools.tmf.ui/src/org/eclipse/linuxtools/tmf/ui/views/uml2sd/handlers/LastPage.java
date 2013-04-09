/**********************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers;

import org.eclipse.jface.action.Action;
import org.eclipse.linuxtools.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDView;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.SDMessages;

/**
 * Action class implementation to move the focus to the last page of the whole sequence diagram.
 *
 * @version 1.0
 * @author Bernd Hufmann
 */
public class LastPage extends Action {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The action ID.
     */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.lastpage"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The sequence diagram view reference
     */
    protected SDView fView = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Standard constructor
     *
     * @param view the view reference
     */
    public LastPage(SDView view) {
        super();
        fView = view;
        setText(SDMessages._141);
        setToolTipText(SDMessages._142);
        setId(ID);
        setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_LAST_PAGE));
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
            fView.getSDPagingProvider().lastPage();
        }
        fView.updateCoolBar();
        fView.getSDWidget().redraw();
    }
}
