/**********************************************************************
 * Copyright (c) 2005, 2008, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: NextPage.java,v 1.3 2008/01/24 02:28:52 apnan Exp $
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
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.SDMessages;

/**
 * @author sveyrier
 * 
 */
public class NextPage extends Action {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.nextpage"; //$NON-NLS-1$
    
    protected SDView fView = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    public NextPage(SDView theView) {
        super();
        fView = theView;
        setText(SDMessages._36);
        setToolTipText(SDMessages._38);
        setId(ID); 
        setImageDescriptor(TmfUiPlugin.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_NEXT_PAGE));
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
            fView.getSDPagingProvider().nextPage();
        }
        fView.updateCoolBar();
        fView.getSDWidget().redraw();
    }
}
