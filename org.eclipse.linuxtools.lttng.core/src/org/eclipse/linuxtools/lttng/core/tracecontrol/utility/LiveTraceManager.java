/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.core.tracecontrol.utility;

import java.util.HashSet;

public class LiveTraceManager {

    private static final HashSet<String> fLiveTraceSet = new HashSet<String>();

    public static void setLiveTrace(String tracePath, boolean live) {
        if (live) {
            fLiveTraceSet.add(tracePath);
        } else {
            fLiveTraceSet.remove(tracePath);
        }
    }

    public static boolean isLiveTrace(String tracePath) {
        return fLiveTraceSet.contains(tracePath);
    }
}
