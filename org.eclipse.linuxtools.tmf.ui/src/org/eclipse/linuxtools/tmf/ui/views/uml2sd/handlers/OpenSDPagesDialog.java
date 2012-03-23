/**********************************************************************
 * Copyright (c) 2005, 2008, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OpenSDPagesDialog.java,v 1.4 2008/01/24 02:28:52 apnan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers;

import org.eclipse.jface.action.Action;
import org.eclipse.linuxtools.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.linuxtools.internal.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDView;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDAdvancedPagingProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.widgets.PagesDialog;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.SDMessages;

/**
 * Action delegate for 'Filter' on a message
 */
public class OpenSDPagesDialog extends Action {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.sdPaging"; //$NON-NLS-1$

    protected SDView fView;
    protected ISDAdvancedPagingProvider fProvider;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * @param view_
     */
    public OpenSDPagesDialog(SDView view_, ISDAdvancedPagingProvider provider_) {
        super(SDMessages._44);
        setImageDescriptor(TmfUiPlugin.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_GOTO_PAGE));
        setId(ID);
        fView = view_;
        fProvider = provider_;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        if (fView == null) {
            return;
        }
        PagesDialog dialog = new PagesDialog(fView, fProvider);
        dialog.open();
    }
}
