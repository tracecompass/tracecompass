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
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.dialogs.PagesDialog;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.handlers.provider.ISDAdvancedPagingProvider;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.util.Messages;

/**
 * Action class implementation for paging.
 *
 * @version 1.0
 * @author Bernd Hufmann
 */
public class OpenSDPagesDialog extends BaseSDAction {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The action ID.
     */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.sdPaging"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The advanced paging provider reference.
     */
    private final ISDAdvancedPagingProvider fProvider;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param view
     *            The view reference
     * @param provider
     *            The provider
     */
    public OpenSDPagesDialog(SDView view, ISDAdvancedPagingProvider provider) {
        super(view);
        setText(Messages.SequenceDiagram_Pages);
        setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_GOTO_PAGE));
        setId(ID);
        fProvider = provider;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void run() {
        if (getView() == null) {
            return;
        }
        PagesDialog dialog = new PagesDialog(getView(), fProvider);
        dialog.open();
    }
}
