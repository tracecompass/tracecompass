/**********************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.tmf.ui.views.uml2sd.handlers;

import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.SDView;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.util.Messages;

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
