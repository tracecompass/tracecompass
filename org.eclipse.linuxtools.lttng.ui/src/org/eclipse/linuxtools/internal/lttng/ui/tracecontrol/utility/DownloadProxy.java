/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Polytechnique Montréal - Initial API and implementation
 *   Bernd Hufmann - Productification, enhancements and fixes
 *   
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.utility;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.TraceResource;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.config.TraceConfig;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.subsystems.TraceSubSystem;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.tm.tcf.protocol.JSON;

/**
 * <b><u>DownloadProxy</u></b>
 * <p>
 * Proxy implementation for storing trace data locally on host where client is running. (writeTraceNetwork)
 * </p>
 */
public class DownloadProxy {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    TraceSubSystem fSubSystem;
    
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *  
     * @param subSystem The trace SubSystem
     */
    public DownloadProxy(TraceSubSystem subSystem) {
        fSubSystem = subSystem;
    }
    
 // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    
    /**
     * Writes trace data to traces files. 
     * 
     * @param data binary data
     */
    public void writeDownloadedTrace(byte[] data) {
        Object[] args = null;
        try {
            args = JSON.parseSequence(data);
        } catch (IOException e) {
            SystemBasePlugin.logError("DownloadProxy", e); //$NON-NLS-1$
        }
        if (args != null) {
            byte[] traceData = JSON.toByteArray(args[4]);
            TraceResource trace = fSubSystem.findTrace(args[0].toString(), args[1].toString(), args[2].toString());
            if (trace != null) {
                TraceConfig conf = trace.getTraceConfig();
                FileOutputStream fos = null;
                if (conf != null && !TraceConfig.InvalidTracePath.equals(conf.getTracePath())) {
                    String fileName = conf.getTracePath() + "/" + args[3].toString();  //$NON-NLS-1$
                    try {
                        fos = new FileOutputStream(fileName, true);
                        fos.write(traceData);
                        fos.close();
                        // ((TraceResource) args[2]).setSize(sizeFile.length());
                    } catch (FileNotFoundException e) {
                        SystemBasePlugin.logError("DownloadProxy", e); //$NON-NLS-1$
                    } catch (IOException e) {
                        SystemBasePlugin.logError("DownloadProxy", e); //$NON-NLS-1$
                    }
                }
            }
        }
    }
    
    /**
     * Method for UST 
     * 
     * @param data
     */
    public void handleUnwriteTraceDataEvent(byte [] data) {
        try {
            Object[] args = null;
            args = JSON.parseSequence(data);
            // Check if it is an ust trace
            TraceResource trace = fSubSystem.findTrace(args[0].toString(), args[1].toString(), args[2].toString());
            if ((trace != null) && trace.isUst()) {
                // TODO
            }

        } catch (IOException e) {
            SystemBasePlugin.logError("DownloadProxy", e); //$NON-NLS-1$
        }
        
    }
    
    /**
     * Method that handles trace done event which is sent at the end of the trace session.
     * 
     * @param data Binary data
     */
    public void handleTraceDoneEvent(byte [] data) {
        // Nothing to do!
    }
}
