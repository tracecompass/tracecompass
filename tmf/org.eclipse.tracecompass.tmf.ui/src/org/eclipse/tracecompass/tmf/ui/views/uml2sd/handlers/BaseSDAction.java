/**********************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.tmf.ui.views.uml2sd.handlers;

import org.eclipse.jface.action.Action;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.SDView;

/**
 * Base class for sequence diagram actions.
 *
 * @author Bernd Hufmann
 */
public class BaseSDAction extends Action {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The sequence diagram view reference.
     */
    private SDView fView = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default constructor
     */
    public BaseSDAction() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param view
     *          a sequence diagram view reference
     */
    public BaseSDAction(SDView view) {
        super();
        fView = view;
    }

    /**
     * Constructor
     * @param view
     *          a sequence diagram view reference
     * @param text
     *          The action text
     * @param style
     *          The style
     */
    protected BaseSDAction(SDView view, String text, int style) {
        super(text, style);
        fView = view;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Sets the active SD view.
     *
     * @param view The SD view.
     */
    public void setView(SDView view) {
        fView = view;
    }

    /**
     * Gets the active SD view.
     *
     * @return view The SD view.
     */
    public SDView getView() {
        return fView;
    }
}
