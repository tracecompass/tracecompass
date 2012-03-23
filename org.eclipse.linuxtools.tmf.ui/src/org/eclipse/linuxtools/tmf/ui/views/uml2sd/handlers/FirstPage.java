/**********************************************************************
 * Copyright (c) 2011 Ericsson
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

import org.eclipse.jface.action.Action;
import org.eclipse.linuxtools.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.linuxtools.internal.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDView;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.SDMessages;

/**
 * Moves the focus on the first page in the sequence diagram view. 
 */
public class FirstPage extends Action {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.firstpage"; //$NON-NLS-1$
    
    protected SDView fView = null;
    
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    public FirstPage(SDView theView) {
        super();
        fView = theView;
        setText(SDMessages._139);
        setToolTipText(SDMessages._140);
        setId(ID);
        setImageDescriptor(TmfUiPlugin.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_FIRST_PAGE));
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
        if ((fView == null) || (fView.getSDWidget()) == null) {
            return;
        }
        if (fView.getSDPagingProvider() != null) {
            fView.getSDPagingProvider().firstPage();
        }
        fView.updateCoolBar();
        fView.getSDWidget().redraw();
    }
}
