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
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.dialogs.FilterListDialog;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDFilterProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.SDMessages;

/**
 * Action class implementation for 'Filtering' of messages/lifelines.
 *
 * @version 1.0
 * @author sveyrier
 */
public class OpenSDFiltersDialog extends Action {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The action ID.
     */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.sdFilters"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The sequence diagram view reference
     */
    protected SDView fView;

    /**
     * The filter provider reference
     */
    protected ISDFilterProvider fProvider;

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
        super(SDMessages._43);
        setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_FILTERS));
        setId(ID);
        setToolTipText(SDMessages._43);
        fView = view;
        fProvider = provider;
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    @Override
    public void run() {
        if (fView == null) {
            return;
        }
        FilterListDialog dialog = new FilterListDialog(fView, fProvider);
        dialog.open();
    }
}
