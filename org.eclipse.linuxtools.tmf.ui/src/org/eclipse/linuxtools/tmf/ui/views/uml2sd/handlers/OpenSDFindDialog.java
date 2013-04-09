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
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.dialogs.SearchFilterDialog;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.SDMessages;
import org.eclipse.swt.SWT;

/**
 * Action class implementation for 'Finding' of messages/lifelines.
 *
 * @version 1.0
 * @author sveyrier
 *
 */
public class OpenSDFindDialog extends Action {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The action ID.
     */
    public final static String ID = "org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.sdFind"; //$NON-NLS-1$
    /**
     * The action definition ID.
     */
    public final static String ACTION_DEFINITION_ID = "org.eclipse.ui.edit.findReplace"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The sequence diagram view reference
     */
    protected SDView fView;

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
        super(SDMessages._41);
        setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_SEARCH_SEQ));
        setId(ID);
        setActionDefinitionId(ACTION_DEFINITION_ID);
        setToolTipText(SDMessages._41);
        fView = view;
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    @Override
    public void run() {
        if (fView == null) {
            return;
        }

        // Disable action while search is ongoing
        this.setEnabled(false);

        try {
            if ((fView.getExtendedFindProvider() != null) && (fView.getExtendedFindProvider().getFindAction() != null)) {
                fView.getExtendedFindProvider().getFindAction().run();
            } else if (fView.getSDFindProvider() != null) {
                SearchFilterDialog dialog = new SearchFilterDialog(fView, fView.getSDFindProvider(), false, SWT.NORMAL);
                dialog.open();
            }
        } finally {
            // Enable action after finishing the search
            this.setEnabled(true);
        }
    }

    /**
     * Sets the active SD view.
     *
     * @param view The SD view.
     */
   public void setView(SDView view) {
        fView = view;
    }
}
