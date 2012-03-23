/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.tmf.ui.project.handlers.messages"; //$NON-NLS-1$

    public static String OpenTraceHandler_Title;
    public static String OpenTraceHandler_NoTraceType;
    public static String OpenTraceHandler_NoTrace;

    public static String OpenExperimentHandler_Title;
    public static String OpenExperimentHandler_NoTraceType;

    public static String DeleteDialog_Title;
    public static String DeleteTraceHandler_Message;
    public static String DeleteExperimentHandler_Message;

    public static String SelectTraceTypeHandler_Title;
    public static String SelectTraceTypeHandler_InvalidTraceType;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
