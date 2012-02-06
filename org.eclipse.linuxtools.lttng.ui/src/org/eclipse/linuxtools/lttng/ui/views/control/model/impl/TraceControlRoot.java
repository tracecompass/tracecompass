/**********************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.control.model.impl;


/**
 * <b><u>TraceControlRoot</u></b>
 * <p>
 * Root element in trace control tree.
 * </p>
 */
public class TraceControlRoot extends TraceControlComponent {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The name of the root component
     */
    public final static String TRACE_CONTROL_ROOT_NAME = "trace_control_root"; //$NON-NLS-1$
    
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
