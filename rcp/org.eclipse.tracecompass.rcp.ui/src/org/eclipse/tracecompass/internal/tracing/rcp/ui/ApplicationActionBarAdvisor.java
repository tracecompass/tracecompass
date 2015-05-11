/**********************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.tracing.rcp.ui;

import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

/**
 * ActionBarAdvisor implementation of the LTTng RCP.
 *
 * @author Bernd Hufmann
 *
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

    // ------------------------------------------------------------------------
    // Constructor(s)
    // ------------------------------------------------------------------------
    /**
     * Default constructor
     *
     * @param configurer
     *          - An action bar configure instance
     */
    public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);
    }

}
