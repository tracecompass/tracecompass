/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.core.tracecontrol;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.lttng.core.tracecontrol.messages"; //$NON-NLS-1$
    
    // Trace resource properties
    public static String Ltt_Trace_Property_TracePathName;
    public static String Ltt_Trace_Property_TracePathDescription;
    public static String Ltt_Trace_Property_NumberOfChannelsName;
    public static String Ltt_Trace_Property_NumberOfChannelsDescr;
    public static String Ltt_Trace_Property_FlighRecorderModeName;
    public static String Ltt_Trace_Property_FlighRecorderModeDesc;
    public static String Ltt_Trace_Property_NormalModeName;
    public static String Ltt_Trace_Property_NormalModeDesc;
    public static String Ltt_Trace_Property_NetworkTraceName;
    public static String Ltt_Trace_Property_NetWorkTraceDescr;
    public static String Ltt_Trace_Property_TraceTransportName;
    public static String Ltt_Trace_Property_TraceTransportDesc;
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
