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
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDView;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.dialogs.MinMaxDialog;
import org.eclipse.ui.IViewPart;

/**
 * Action class implementation to configure minimum and maximum time range values.
 *
 * @version 1.0
 * @author sveyrier
 *
 */
public class ConfigureMinMax extends Action {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The corresponding sequence diagram view reference.
     */
    protected SDView fView = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Standard constructor.
     *
     * @param view The sequence diagram view for the action
     */
    public ConfigureMinMax(IViewPart view) {
        super();
        if (view instanceof SDView) {
            fView = (SDView) view;
        }
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    @Override
    public void run() {
        if ((fView != null) && (fView.getSDWidget() != null)) {
            MinMaxDialog minMax = new MinMaxDialog(fView.getSite().getShell(), fView.getSDWidget());
            minMax.open();
        }
    }
}
