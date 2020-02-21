/**********************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
