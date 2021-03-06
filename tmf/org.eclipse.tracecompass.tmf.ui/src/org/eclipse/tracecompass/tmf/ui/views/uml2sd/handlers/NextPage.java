/**********************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation, Ericsson
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Bernd Hufmann - Updated for TMF
 **********************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.uml2sd.handlers;

import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.SDView;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.util.Messages;

/**
 * Action class implementation to move the focus to the next page of the whole sequence diagram.
 *
 * @version 1.0
 * @author sveyrier
 *
 */
public class NextPage extends BaseSDAction {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The action ID.
     */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.nextpage"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default constructor
     *
     * @param view the view reference
     */
    public NextPage(SDView view) {
        super(view);
        setText(Messages.SequenceDiagram_NextPage);
        setToolTipText(Messages.SequenceDiagram_GoToNextPage);
        setId(ID);
        setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_NEXT_PAGE));
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

     @Override
    public void run() {
        if ((getView() == null) || (getView().getSDWidget()) == null) {
            return;
        }
        if (getView().getSDPagingProvider() != null) {
            getView().getSDPagingProvider().nextPage();
        }
        getView().updateCoolBar();
        getView().getSDWidget().redraw();
    }
}
