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

import org.eclipse.tracecompass.tmf.ui.views.uml2sd.SDView;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.dialogs.MinMaxDialog;

/**
 * Action class implementation to configure minimum and maximum time range values.
 *
 * @author sveyrier
 */
public class ConfigureMinMax extends BaseSDAction {

    /**
     * Constructor
     * @param view
     *          the sequence diagram view reference
     */
    public ConfigureMinMax(SDView view) {
        super(view);
    }
    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------
    @Override
    public void run() {
        if ((getView() != null) && (getView().getSDWidget() != null)) {
            MinMaxDialog minMax = new MinMaxDialog(getView().getSite().getShell(), getView().getSDWidget());
            minMax.open();
        }
    }
}
