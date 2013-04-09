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
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDView;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.dialogs.PagesDialog;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDAdvancedPagingProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.SDMessages;

/**
 * Action class implementation for paging.
 *
 * @version 1.0
 * @author Bernd Hufmann
 */
public class OpenSDPagesDialog extends Action {

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
     * The sequence diagram view reference
     */
    protected SDView fView;
    /**
     * The advanced paging provider reference.
     */
    protected ISDAdvancedPagingProvider fProvider;

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
        super(SDMessages._44);
        setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_GOTO_PAGE));
        setId(ID);
        fView = view;
        fProvider = provider;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void run() {
        if (fView == null) {
            return;
        }
        PagesDialog dialog = new PagesDialog(fView, fProvider);
        dialog.open();
    }
}
