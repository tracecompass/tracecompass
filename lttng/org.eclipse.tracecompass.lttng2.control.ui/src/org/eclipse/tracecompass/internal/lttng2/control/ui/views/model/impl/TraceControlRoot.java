/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
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
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl;


/**
 * <p>
 * Root element in trace control tree.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class TraceControlRoot extends TraceControlComponent {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The name of the root component
     */
    public static final String TRACE_CONTROL_ROOT_NAME = "trace_control_root"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default constructor
     */
    public TraceControlRoot() {
        super(TRACE_CONTROL_ROOT_NAME);
    }

}
