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
package org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.config;

import org.eclipse.core.resources.IProject;


/**
 * <b><u>TraceChannel</u></b>
 * <p>
 *  This models a trace representing a trace resource for a particular remote system.
 * </p>
 */
public class TraceConfig {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
	public static final int NORMAL_MODE = 0;
	public static final int FLIGHT_RECORDER_MODE = 1;
	
	public static final String InvalidTracePath = "network"; //$NON-NLS-1$
	
    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private String fTraceName = null;
    private String fTraceTransport = null;
    private String fTracePath = null;
    private boolean fIsNetworkTrace = false;
    private boolean fIsAppend = false;
    private int fMode = 0;
    private int fNumChannel = 0;
    private TraceChannels fChannels = null;
    private IProject fProject = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    public TraceConfig() {
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Gets the number of threads
     * 
     * @return number of threads
     */
    public int getNumChannel() {
        return fNumChannel;
    }

    /**
     * Sets the number of threads (channels)
     * @param numChannel
     */
    public void setNumChannel(int numChannel) {
        fNumChannel = numChannel;
    }

    /**
     * Gets the trace name.
     * 
     * @return trace name
     */
    public String getTraceName() {
        return fTraceName;
    }

    /**
     * Sets the trace name.
     * 
     * @param traceName
     */
    public void setTraceName(String traceName) {
        fTraceName = traceName;
    }

    /** 
     * Gets the trace transport.
     * 
     * @return trace transport
     */
    public String getTraceTransport() {
        return fTraceTransport;
    }

    /**
     * Sets the trace transport.
     * 
     * @param traceTransport
     */
    public void setTraceTransport(String traceTransport) {
        fTraceTransport = traceTransport;
    }

    /**
     * Returns wether trace is a network trace (i.e. trace will be stored 
     * on local host where client resides) or a local trace (i.e. trace will
     * be stored on same machine where the actual trace is collected) 
     * 
     * @return isNetworktrace
     */
    public boolean isNetworkTrace() {
        return fIsNetworkTrace;
    }

    /**
     * Sets whether trace is a network trace (i.e. trace will be stored 
     * on local host where client resides) or a local trace (i.e. trace will
     * be stored on same machine where the actual trace is collected) 
     * 
     * @param isNetworkTrace
     */
    public void setNetworkTrace(boolean isNetworkTrace) {
        fIsNetworkTrace = isNetworkTrace;
    }

    /**
     * Returns whether trace should append an existing trace or not.
     *  
     * @return true if append else false
     */
    public boolean getIsAppend() {
        return fIsAppend;
    }

    /**
     * Sets whether trace should append an existing trace or not.
     * 
     * @param isAppend
     */
    public void setIsAppend(boolean isAppend) {
        fIsAppend = isAppend;
    }

    /** 
     * Gets the trace mode.
     * 
     * @return trace mode
     */
    public int getMode() {
        return fMode;
    }

    /** 
     * Sets the trace mode.
     * 
     * @param mode
     */
    public void setMode(int mode) {
        fMode = mode;
    }

    /**
     * Gets the path where trace will be stored.
     * 
     * @return trace path
     */
    public String getTracePath() {
        return fTracePath;
    }

    /**
     * Sets the path where trace will be stored.
     * 
     * @param path
     */
    public void setTracePath(String path) {
        fTracePath = path;
    }
    
    /**
     * Gets the trace channels collection.
     * 
     * @return trace channels
     */
    public TraceChannels getTraceChannels() {
        return fChannels;
    }
    
    /**
     * Sets the trace channels collection.
     * 
     * @param channels
     */
    public void setTraceChannels(TraceChannels channels) {
        fChannels = channels;
    }
    
    /**
     * Sets the trace channels collection with given names
     * and creates default trace channels.
     * 
     * @param channels
     */
    public void setTraceChannels(String[] channels) {
        fChannels = new TraceChannels();
        fChannels.putAll(channels);
    }
    
    /**
     * Gets the trace project.
     * 
     * @return project
     */
    public IProject getProject() {
        return fProject;
    }
    
    /**
     * Sets the trace project.
     * 
     * @param project
     */
    public void setProject(IProject project) {
        fProject = project;
    }
    
}
