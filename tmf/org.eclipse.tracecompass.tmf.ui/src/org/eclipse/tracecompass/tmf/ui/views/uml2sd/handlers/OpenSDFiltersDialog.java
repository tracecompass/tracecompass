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
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.dialogs.FilterListDialog;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.handlers.provider.ISDFilterProvider;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.util.Messages;

/**
 * Action class implementation for 'Filtering' of messages/lifelines.
 *
 * @version 1.0
 * @author sveyrier
 */
public class OpenSDFiltersDialog extends BaseSDAction {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The action ID.
     */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.sdFilters"; //$NON-NLS-1$

    /**
     * The filter provider reference
     */
    private final ISDFilterProvider fProvider;

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
    public OpenSDFiltersDialog(SDView view, ISDFilterProvider provider) {
        super(view);
        setText(Messages.SequenceDiagram_HidePatterns);
        setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_FILTERS));
        setId(ID);
        setToolTipText(Messages.SequenceDiagram_HidePatterns);
        fProvider = provider;
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    @Override
    public void run() {
        if (getView() == null) {
            return;
        }
        FilterListDialog dialog = new FilterListDialog(getView(), fProvider);
        dialog.open();
    }
}
