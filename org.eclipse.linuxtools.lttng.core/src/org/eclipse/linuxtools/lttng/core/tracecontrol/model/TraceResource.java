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
package org.eclipse.linuxtools.lttng.core.tracecontrol.model;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.linuxtools.lttng.core.tracecontrol.Messages;
import org.eclipse.linuxtools.lttng.core.tracecontrol.model.config.TraceConfig;
import org.eclipse.linuxtools.lttng.core.tracecontrol.service.ILttControllerService;
import org.eclipse.rse.core.subsystems.AbstractResource;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.util.TCFTask;

/**
 * <b><u>ProviderResource</u></b>
 * <p>
 * This models a remote resource representing a trace defined on a particular system.
 * </p>
 */
public class TraceResource extends AbstractResource implements Comparable<TraceResource> {

    
    public static enum TraceState { CREATED, CONFIGURED, STARTED, PAUSED, STOPPED };
    
    public static final String Ltt_Trace_Property_TracePath = "trace_path"; //$NON-NLS-1$
    public static final String Ltt_Trace_Property_TraceNumberOfChannels = "num_threads"; //$NON-NLS-1$
    public static final String Ltt_Trace_Property_FlightRecorderMode = "flight_only"; //$NON-NLS-1$
    public static final String Ltt_Trace_Property_NormalMode = "normal_only"; //$NON-NLS-1$
    public static final String Ltt_Trace_Property_NetworkTrace = "isNetwork"; //$NON-NLS-1$
    public static final String Ltt_Trace_Property_TraceTransport = "transport"; //$NON-NLS-1$
    
    public static final int DEFAULT_TCF_TASK_TIMEOUT = 10;

    private static final Map<String, PropertyInfo> fPropertyInfo = new HashMap<String, PropertyInfo>();
    
    static {
        fPropertyInfo.put(Ltt_Trace_Property_TracePath, new PropertyInfo(Messages.Ltt_Trace_Property_TracePathName, Messages.Ltt_Trace_Property_TracePathDescription));
        fPropertyInfo.put(Ltt_Trace_Property_TraceNumberOfChannels, new PropertyInfo(Messages.Ltt_Trace_Property_NumberOfChannelsName, Messages.Ltt_Trace_Property_NumberOfChannelsDescr));
        fPropertyInfo.put(Ltt_Trace_Property_FlightRecorderMode, new PropertyInfo(Messages.Ltt_Trace_Property_FlighRecorderModeName, Messages.Ltt_Trace_Property_FlighRecorderModeDesc));
        fPropertyInfo.put(Ltt_Trace_Property_NormalMode, new PropertyInfo(Messages.Ltt_Trace_Property_NormalModeName, Messages.Ltt_Trace_Property_NormalModeDesc));
        fPropertyInfo.put(Ltt_Trace_Property_NetworkTrace, new PropertyInfo(Messages.Ltt_Trace_Property_NetworkTraceName, Messages.Ltt_Trace_Property_NetWorkTraceDescr));
        fPropertyInfo.put(Ltt_Trace_Property_TraceTransport, new PropertyInfo(Messages.Ltt_Trace_Property_TraceTransportName, Messages.Ltt_Trace_Property_TraceTransportDesc));
    }

    public static class PropertyInfo {
        private final String name;
        private final String description;
        PropertyInfo(String name, String description) {
            this.name = name;
            this.description = description;
        }
        public String getName() {
            return name;
        }
        public String getDescription() {
            return description;
        }
    }

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    
    private String fName;
    private String fId;
    private TargetResource fParent;
    private TraceState fTraceState;
    private TraceConfig fTraceConfig;
    private ILttControllerService fService;
    
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor for TraceResource when given fParent subsystem.
     */
    public TraceResource(ISubSystem parentSubSystem, ILttControllerService service) {
        super(parentSubSystem);
        fService = service;
    }
    
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Returns the trace state.
     */
    public TraceState getTraceState() {
    	return fTraceState;
    }
    
    /**
     * Sets the trace state.
     * 
     * @param traceState The new state.
     */
    public void setTraceState(TraceState traceState) {
    	fTraceState = traceState;
    }

    /**
     * Returns the trace configuration for this trace.
     * 
     * @return traceConfig
     */
    public TraceConfig getTraceConfig() {
    	return fTraceConfig;
    }

    /**
     * Sets the trace configuration for this trace.
     * 
     * @param traceConfig
     */
    public void setTraceConfig(TraceConfig traceConfig) {
    	fTraceConfig = traceConfig;
    }
    
    /**
     * Returns the name of the trace resource.
     * 
     * @return String
     */
    public String getName() {
        return fName;
    }

    /**
     * Sets the name of the trace resource.
     * 
     * @param fName The fName to set
     */
    public void setName(String name) {
        fName = name;
    }

    /**
     * Returns the ID of the trace resource.
     * 
     * @return String
     */
    public String getId() {
        return fId;
    }

    /**
     * Sets the ID of the trace resource.
     * 
     * @param fId The fId to set
     */
    public void setId(String id) {
        fId = id;
    }

    /**
     * Returns the parent target resource.
     * @return
     */
    public TargetResource getParent() {
        return fParent;
    }

    /**
     * Sets the parent target resource.
     * 
     * @param target
     */
    public void setParent(TargetResource target) {
        fParent = target;
    }
    
    /**
     * Returns the property information for this trace.
     * 
     * @return the value
     */
    public Map<String,PropertyInfo> getPropertyInfo() {
        return fPropertyInfo;
    }

    /**
     * Gets the property information for a given property name. 
     *  
     * @param property the property to get
     * 
     * @return the value
     */
    public String getProperty(String property) {
        if ((fTraceConfig != null) && (fPropertyInfo.containsKey(property))) {
            if (Ltt_Trace_Property_TracePath.equals(property)) {
                return fTraceConfig.getTracePath();
            }
            else if (Ltt_Trace_Property_TraceNumberOfChannels.equals(property)) {
                return String.valueOf(fTraceConfig.getNumChannel());
            }
            else if (Ltt_Trace_Property_FlightRecorderMode.equals(property)) {
                return String.valueOf(fTraceConfig.getMode() == TraceConfig.FLIGHT_RECORDER_MODE);
            }
            else if (Ltt_Trace_Property_NormalMode.equals(property)) {
                return String.valueOf(fTraceConfig.getMode() == TraceConfig.NORMAL_MODE);
            }
            else if (Ltt_Trace_Property_NetworkTrace.equals(property)) {
                return String.valueOf(fTraceConfig.isNetworkTrace());
            }
            else if (Ltt_Trace_Property_TraceTransport.equals(property)) {
                return String.valueOf(fTraceConfig.getTraceTransport());
            }
        }
        return ""; //$NON-NLS-1$
    }
    
    /**
     * @return true if the trace is a network trace and has been already started.
     */
    public boolean isNetworkTraceAndStarted () {
        // for network traces, if trace path is available and if state is started
        return (fTraceConfig != null) && fTraceConfig.isNetworkTrace() && 
               !(TraceConfig.InvalidTracePath.equals(fTraceConfig.getTracePath())) &&
               (fTraceState == TraceState.STARTED);
    }
    
    /**
     * Returns whether the trace is a UST or kernel trace. 
     * 
     * @return true if UST, false for kernel 
     */
    public boolean isUst() {
        return fParent.isUst(); 
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        
        if (this == other) {
            return true;
        }

        // We only check the name because the trace name has to be unique
        if (other instanceof TraceResource) {
            TraceResource otherTrace = (TraceResource) other;
            if (fName != null) {
                return fName.equals(otherTrace.fName);    
            }
        }
        return false;        
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override 
    public int hashCode() {
        // We only use the name because the trace name has to be unique
        return fName.hashCode();
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(TraceResource o) {
        // We only check the name because the trace name has to be unique
        return fName.toLowerCase().compareTo(o.fName.toLowerCase());
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "[TraceResource (" + fName + ")]";
    }

    /*
     * Setup trace on the remote system. 
     */
    public void setupTrace() throws Exception {
        // Create future task
        new TCFTask<Boolean>() {
            @Override
            public void run() {

                // Setup trace using Lttng controller service proxy
                fService.setupTrace(fParent.getParent().getName(),
                        fParent.getName(), 
                        fName, 
                        new ILttControllerService.DoneSetupTrace() {

                    @Override
                    public void doneSetupTrace(IToken token, Exception error, Object str) {
                        if (error != null) {
                            // Notify with error
                            error(error);
                            return;
                        }

                        // Notify about success
                        done(Boolean.valueOf(true));
                    }
                });
            }}.get(DEFAULT_TCF_TASK_TIMEOUT, TimeUnit.SECONDS);
    }

    /*
     * Enable or disable a channel on the remote system. 
     */
    public void setChannelEnable(final String channelName, final boolean enabled) throws Exception {
        // Create future task
        new TCFTask<Boolean>() {
            @Override
            public void run() {

                // Set marker enable using Lttng controller service proxy
                fService.setChannelEnable(fParent.getParent().getName(),
                        fParent.getName(), 
                        fName, 
                        channelName, 
                        enabled,  
                        new ILttControllerService.DoneSetChannelEnable() {

                    @Override
                    public void doneSetChannelEnable(IToken token, Exception error, Object str) {
                        if (error != null) {
                            // Notify with error
                            error(error);
                            return;
                        }

                        // Notify about success
                        done(Boolean.valueOf(true));
                    }
                });
            }}.get(DEFAULT_TCF_TASK_TIMEOUT, TimeUnit.SECONDS);
    }

    /*
     * Set channel overwrite on the remote system. 
     */
    public void setChannelOverwrite(final String channelName, final boolean overwrite) throws Exception {
        // Create future task
       new TCFTask<Boolean>() {
            @Override
            public void run() {

                // Set marker overwrite using Lttng controller service proxy
                fService.setChannelOverwrite(fParent.getParent().getName(),
                        fParent.getName(), 
                        fName, 
                        channelName, 
                        overwrite,  
                        new ILttControllerService.DoneSetChannelOverwrite() {

                    @Override
                    public void doneSetChannelOverwrite(IToken token, Exception error, Object str) {
                        if (error != null) {
                            // Notify with error
                            error(error);
                            return;
                        }

                        // Notify about success
                        done(Boolean.valueOf(true));
                    }
                });
            }}.get(DEFAULT_TCF_TASK_TIMEOUT, TimeUnit.SECONDS);
    }

    /*
     * Set channel timer on the remote system. 
     */
    public void setChannelTimer(final String channelName, final long timer) throws Exception {
        // Create future task
       new TCFTask<Boolean>() {
            @Override
            public void run() {

                // Set marker switch_timer using Lttng controller service proxy
                fService.setChannelTimer(fParent.getParent().getName(),
                        fParent.getName(), 
                        fName, 
                        channelName, 
                        timer,  
                        new ILttControllerService.DoneSetChannelTimer() {

                    @Override
                    public void doneSetChannelTimer(IToken token, Exception error, Object str) {
                        if (error != null) {
                            // Notify with error
                            error(error);
                            return;
                        }

                        // Notify about success
                        done(Boolean.valueOf(true));
                    }
                });
            }}.get(DEFAULT_TCF_TASK_TIMEOUT, TimeUnit.SECONDS);
    }

    /*
     * Setup the size of the sub-buffer on the remote system.
     */
    public void setChannelSubbufNum(final String channelName, final long subbufNum) throws Exception {
        // Create future task
        new TCFTask<Boolean>() {
            @Override
            public void run() {

                // Set marker enable using Lttng controller service proxy
                fService.setChannelSubbufNum(fParent.getParent().getName(),
                        fParent.getName(),
                        fName,
                        channelName,
                        subbufNum,
                        new ILttControllerService.DoneSetChannelSubbufNum() {

                    @Override
                    public void doneSetChannelSubbufNum(IToken token, Exception error, Object str) {
                        if (error != null) {
                            // Notify with error
                            error(error);
                            return;
                        }

                        // Notify about success
                        done(Boolean.valueOf(true));
                    }
                });
            }}.get(DEFAULT_TCF_TASK_TIMEOUT, TimeUnit.SECONDS);
    }

    /*
     * Setup the size of the sub-buffer on the remote system.
     */
    public void setChannelSubbufSize(final String channelName, final long subbufSize) throws Exception {
        // Create future task
        new TCFTask<Boolean>() {
            @Override
            public void run() {

                // Set marker enable using Lttng controller service proxy
                fService.setChannelSubbufSize(fParent.getParent().getName(),
                        fParent.getName(), 
                        fName, 
                        channelName, 
                        subbufSize,  
                        new ILttControllerService.DoneSetChannelSubbufSize() {

                    @Override
                    public void doneSetChannelSubbufSize(IToken token, Exception error, Object str) {
                        if (error != null) {
                            // Notify with error
                            error(error);
                            return;
                        }

                        // Notify about success
                        done(Boolean.valueOf(true));
                    }
                });
            }}.get(DEFAULT_TCF_TASK_TIMEOUT, TimeUnit.SECONDS);
    }


}
