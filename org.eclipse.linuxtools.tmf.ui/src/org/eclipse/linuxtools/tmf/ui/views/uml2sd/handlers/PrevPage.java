/**********************************************************************
 * Copyright (c) 2005, 2008, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: PrevPage.java,v 1.3 2008/01/24 02:28:52 apnan Exp $
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
public class PrevPage extends Action {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    public final static String ID = "org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.prevpage"; //$NON-NLS-1$
    
    protected SDView view = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    public PrevPage(SDView theView) {
        super();
        view = theView;
        setText(SDMessages._35);
        setToolTipText(SDMessages._37);
        setId(ID); 
        setImageDescriptor(TmfUiPlugin.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_PREV_PAGE));
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
        if ((view == null) || (view.getSDWidget()) == null) {
            return;
        }
        if (view.getSDPagingProvider() != null) {
            view.getSDPagingProvider().prevPage();
        }
        view.updateCoolBar();
        view.getSDWidget().redraw();
    }
}
