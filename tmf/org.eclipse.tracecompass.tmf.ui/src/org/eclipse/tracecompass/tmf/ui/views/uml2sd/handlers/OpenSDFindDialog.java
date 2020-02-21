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

import org.eclipse.swt.SWT;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.SDView;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.dialogs.SearchFilterDialog;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.util.Messages;

/**
 * Action class implementation for 'Finding' of messages/lifelines.
 *
 * @version 1.0
 * @author sveyrier
 *
 */
public class OpenSDFindDialog extends BaseSDAction {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The action ID.
     */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.sdFind"; //$NON-NLS-1$

    /**
     * The action definition ID.
     */
    public static final String ACTION_DEFINITION_ID = "org.eclipse.ui.edit.findReplace"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default constructor
     */
    public OpenSDFindDialog() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param view The view reference
     */
    public OpenSDFindDialog(SDView view) {
        super(view);
        setText(Messages.SequenceDiagram_Find + "..."); //$NON-NLS-1$
        setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_SEARCH_SEQ));
        setId(ID);
        setActionDefinitionId(ACTION_DEFINITION_ID);
        setToolTipText(Messages.SequenceDiagram_Find + "..."); //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    @Override
    public void run() {
        if (getView() == null) {
            return;
        }

        // Disable action while search is ongoing
        this.setEnabled(false);

        try {
            if ((getView().getExtendedFindProvider() != null) && (getView().getExtendedFindProvider().getFindAction() != null)) {
                getView().getExtendedFindProvider().getFindAction().run();
            } else if (getView().getSDFindProvider() != null) {
                SearchFilterDialog dialog = new SearchFilterDialog(getView(), getView().getSDFindProvider(), false, SWT.NORMAL);
                dialog.open();
            }
        } finally {
            // Enable action after finishing the search
            this.setEnabled(true);
        }
    }
}
