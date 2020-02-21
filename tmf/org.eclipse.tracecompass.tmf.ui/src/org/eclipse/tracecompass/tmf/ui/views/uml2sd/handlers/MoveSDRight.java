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
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.SDWidget;

/**
 * Action class implementation to move right in the sequence diagram view within a page.
 *
 * @version 1.0
 * @author sveyrier
 *
 */

public class MoveSDRight extends BaseSDAction {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The action ID.
     */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.MoveSDRight"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default constructor
     */
    public MoveSDRight() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param view a sequence diagram view reference
     */
    public MoveSDRight(SDView view) {
        super(view);
        setId(ID);
        setActionDefinitionId(ID);
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    @Override
    public void run() {

        if (getView() == null) {
            return;
        }

        SDWidget viewer = getView().getSDWidget();
        if (viewer != null) {
            viewer.scrollBy(+viewer.getVisibleWidth(), 0);
        }
    }
}
