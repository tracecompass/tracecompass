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

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.lttng.ui.views.control.Messages;
import org.eclipse.linuxtools.lttng.ui.views.control.model.IChannelInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.IDomainInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.ITraceControlComponent;
import org.eclipse.linuxtools.lttng.ui.views.control.model.LogLevelType;
import org.eclipse.linuxtools.lttng.ui.views.control.model.TraceLogLevel;
import org.eclipse.linuxtools.lttng.ui.views.control.property.TraceDomainPropertySource;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * <b><u>TraceDomainComponent</u></b>
 * <p>
 * Implementation of the trace domain component.
 * </p>
 */
public class TraceDomainComponent extends TraceControlComponent {
    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * Path to icon file for this component.
     */
    public static final String TRACE_DOMAIN_ICON_FILE = "icons/obj16/domain.gif"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The domain information.
     */
    private IDomainInfo fDomainInfo = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor 
     * @param name - the name of the component.
     * @param parent - the parent of this component.
     */
    public TraceDomainComponent(String name, ITraceControlComponent parent) {
        super(name, parent);
        setImage(TRACE_DOMAIN_ICON_FILE);
        setToolTip(Messages.TraceControl_DomainDisplayName);
        fDomainInfo = new DomainInfo(name);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * Sets the domain information.
     * @param domainInfo - the domain information to set.
     */
    public void setDomainInfo(IDomainInfo domainInfo) {
        fDomainInfo = domainInfo;
        IChannelInfo[] channels = fDomainInfo.getChannels();
        for (int i = 0; i < channels.length; i++) {
            TraceChannelComponent channel = new TraceChannelComponent(channels[i].getName(), this);
            channel.setChannelInfo(channels[i]);
            addChild(channel);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TraceControlComponent#getAdapter(java.lang.Class)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == IPropertySource.class) {
            return new TraceDomainPropertySource(this);
        }
        return null;
    }
    
    /**
     * @return session name from parent
     */
    public String getSessionName() {
        return ((TraceSessionComponent)getParent()).getName();
    }
    
    /**
     * @return session from parent
     */
    public TraceSessionComponent getSession() {
       return (TraceSessionComponent)getParent(); 
    }

    /**
     * @return true if domain is kernel, false for UST
     */
    public boolean isKernel() {
        return fDomainInfo.isKernel();
    }
    
    /**
     * Sets whether domain is  Kernel domain or UST 
     * @param isKernel true for kernel, false for UST
     */
    public void setIsKernel(boolean isKernel) {
        fDomainInfo.setIsKernel(isKernel);
    }
    
    /**
     * @return returns all available channels for this domain.
     */
    public TraceChannelComponent[] getChannels() {
        List<ITraceControlComponent> channels = getChildren(TraceChannelComponent.class);
        return (TraceChannelComponent[])channels.toArray(new TraceChannelComponent[channels.size()]);
    }
    
    /**
     * @return the parent target node
     */
    public TargetNodeComponent getTargetNode() {
        return ((TraceSessionComponent)getParent()).getTargetNode();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Retrieves the session configuration from the node. 
     * @throws ExecutionException
     */
    public void getConfigurationFromNode() throws ExecutionException {
        getConfigurationFromNode(new NullProgressMonitor());
    }
    /**
     * Retrieves the session configuration from the node. 
     * @param monitor - a progress monitor
     * @throws ExecutionException
     */
    public void getConfigurationFromNode(IProgressMonitor monitor) throws ExecutionException {
        TraceSessionComponent session = (TraceSessionComponent) getParent();
        session.getConfigurationFromNode(monitor);
    }
    /**
     * Enables channels with given names which are part of this domain. If a given channel 
     * doesn't exists it creates a new channel with the given parameters (or default values 
     * if given parameter is null). 
     * @param channelNames - a list of channel names to enable on this domain
     * @param info - channel information to set for the channel (use null for default)
     * @throws ExecutionException
     */
    public void enableChannels(List<String> channelNames, IChannelInfo info) throws ExecutionException {
        enableChannels(channelNames, info, new NullProgressMonitor());
    }
    /**
     * Enables channels with given names which are part of this domain. If a given channel 
     * doesn't exists it creates a new channel with the given parameters (or default values 
     * if given parameter is null). 
     * @param channelNames - a list of channel names to enable on this domain
     * @param info - channel information to set for the channel (use null for default)
     * @param monitor - a progress monitor
     * @throws ExecutionException
     */
    public void enableChannels(List<String> channelNames, IChannelInfo info, IProgressMonitor monitor) throws ExecutionException {
        getControlService().enableChannels(getParent().getName(), channelNames, isKernel(), info, monitor);
    }
    /**
     * Disables channels with given names which are part of this domain. 
     * @param channelNames - a list of channel names to enable on this domain
     * @throws ExecutionException
     */
    public void disableChannels(List<String> channelNames) throws ExecutionException {
        disableChannels(channelNames, new NullProgressMonitor());
    }
    /**
     * Disables channels with given names which are part of this domain. 
     * @param channelNames - a list of channel names to enable on this domain
     * @param monitor - a progress monitor
     * @throws ExecutionException
     */
    public void disableChannels(List<String> channelNames, IProgressMonitor monitor) throws ExecutionException {
        getControlService().disableChannels(getParent().getName(), channelNames, isKernel(), monitor);
    }

    /**
     * Enables a list of events with no additional parameters.
     * @param eventNames - a list of event names to enabled.
     * @param monitor - a progress monitor
     * @throws ExecutionException
     */
    public void enableEvents(List<String> eventNames, IProgressMonitor monitor) throws ExecutionException {
        getControlService().enableEvents(getSessionName(), null, eventNames, isKernel(), monitor);
    }

    /**
     * Enables all syscalls (for kernel domain)
     * @throws ExecutionException
     */
   public void enableSyscalls() throws ExecutionException {
        enableSyscalls(new NullProgressMonitor());
    }

   /**
    * Enables all syscalls (for kernel domain)
    * @param monitor - a progress monitor
    * @throws ExecutionException
    */

    public void enableSyscalls(IProgressMonitor monitor) throws ExecutionException {
        getControlService().enableSyscalls(getSessionName(), null, monitor);
    }

    /**
     * Enables a dynamic probe (for kernel domain)
     * @param eventName - event name for probe
     * @param isFunction - true for dynamic function entry/return probe else false
      * @param probe - the actual probe
     * @throws ExecutionException
     */
    public void enableProbe(String eventName, boolean isFunction, String probe) throws ExecutionException {
        enableProbe(eventName, isFunction, probe, new NullProgressMonitor());
    }
    
    /**
     * Enables a dynamic probe (for kernel domain)
     * @param eventName - event name for probe
     * @param isFunction - true for dynamic function entry/return probe else false
     * @param probe - the actual probe
     * @param monitor - a progress monitor
     * @throws ExecutionException
     */
    public void enableProbe(String eventName, boolean isFunction, String probe, IProgressMonitor monitor) throws ExecutionException {
        getControlService().enableProbe(getSessionName(), null, eventName, isFunction, probe, monitor);
    }

    /**
     * Enables events using log level.
     * @param eventName - a event name
     * @param logLevelType - a log level type 
     * @param level - a log level 
     * @throws ExecutionException
     */
    public void enableLogLevel(String eventName, LogLevelType logLevelType, TraceLogLevel level) throws ExecutionException {
        enableLogLevel(eventName, logLevelType, level, new NullProgressMonitor());
    }

    /**
     * Enables events using log level.
     * @param eventName - a event name
     * @param logLevelType - a log level type 
     * @param level - a log level 
     * @param monitor - a progress monitor  
     * @throws ExecutionException
     */
    public void enableLogLevel(String eventName, LogLevelType logLevelType, TraceLogLevel level, IProgressMonitor monitor) throws ExecutionException {
        getControlService().enableLogLevel(getSessionName(), null, eventName, logLevelType, level, monitor);
    }

}
