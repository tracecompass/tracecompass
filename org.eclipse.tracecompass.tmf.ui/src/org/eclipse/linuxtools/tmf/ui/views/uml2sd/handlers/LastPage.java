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

import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDView;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.Messages;

/**
 * Action class implementation to move the focus to the last page of the whole sequence diagram.
 *
 * @version 1.0
 * @author Bernd Hufmann
 */
public class LastPage extends BaseSDAction {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The action ID.
     */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.lastpage"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Standard constructor
     *
     * @param view the view reference
     */
    public LastPage(SDView view) {
        super(view);
        setText(Messages.SequenceDiagram_LastPage);
        setToolTipText(Messages.SequenceDiagram_GoToLastPage);
        setId(ID);
        setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_LAST_PAGE));
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    @Override
    public void run() {
        if ((getView() == null) || (getView().getSDWidget()) == null) {
            return;
        }
        if (getView().getSDPagingProvider() != null) {
            getView().getSDPagingProvider().lastPage();
        }
        getView().updateCoolBar();
        getView().getSDWidget().redraw();
    }
}
