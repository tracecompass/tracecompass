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
package org.eclipse.linuxtools.lttng.ui.views.control.service;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.lttng.ui.views.control.model.IBaseEventInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.IChannelInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.ISessionInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.IUstProviderInfo;


/** <b><u>ILttngControlService</u></b>
* <p>
* Interface for LTTng trace control command service. 
* </p>
*/
public interface ILttngControlService {
    /**
     * Retrieves the existing sessions names from the node.
     * @param monitor - a progress monitor
     * @return an array with session names.
     * @throws ExecutionException
     */
    public String[] getSessionNames(IProgressMonitor monitor) throws ExecutionException;
    
    /**
     * Retrieves the session information with the given name the node.
     * @param sessionName - the session name
     * @param monitor - a progress monitor 
     * @return session information
     * @throws ExecutionException
     */    
    public ISessionInfo getSession(String sessionName, IProgressMonitor monitor) throws ExecutionException;
    
    /**
     * Retrieves the kernel provider information (i.e. the kernel events)
     * @param monitor - a progress monitor 
     * @return the list of existing kernel events.
     * @throws ExecutionException
     */
    public List<IBaseEventInfo> getKernelProvider(IProgressMonitor monitor) throws ExecutionException;
    
    /**
     * Retrieves the UST provider information from the node.
     * @return - the UST provider information.
     * @throws ExecutionException
     */
    public List<IUstProviderInfo> getUstProvider() throws ExecutionException;
    /**
     * Retrieves the UST provider information from the node.
     * @param monitor - a progress monitor 
     * @return the UST provider information.
     * @throws ExecutionException
     */
    public List<IUstProviderInfo> getUstProvider(IProgressMonitor monitor) throws ExecutionException;
    
    /**
     * Creates a session with given session name and location.
     * @param sessionName - a session name to create
     * @param sessionPath - a path for storing the traces (use null for default)
     * @param monitor - a progress monitor 
     * @return the session information
     * @throws ExecutionException
     */
    public ISessionInfo createSession(String sessionName, String sessionPath, IProgressMonitor monitor) throws ExecutionException;
    
    /**
     * Destroys a session with given session name. 
     * @param sessionName - a session name to destroy
     * @param monitor - a progress monitor 
     * @throws ExecutionException
     */
    public void destroySession(String sessionName, IProgressMonitor monitor) throws ExecutionException;

    /**
     * Starts a session with given session name. 
     * @param sessionName - a session name to start
     * @param monitor - a progress monitor 
     * @throws ExecutionException
     */    
    public void startSession(String sessionName, IProgressMonitor monitor) throws ExecutionException;

     /**
      * Stops a session with given session name. 
      * @param sessionName - a session name to stop
      * @param monitor - a progress monitor 
      * @throws ExecutionException
      */
     public void stopSession(String sessionName, IProgressMonitor monitor) throws ExecutionException;

    
     /**
      * Enables a list of channels for given session and given channel information (configuration). 
      * @param sessionName - a session name to create
      * @param channelNames - a list of channel names to enable
      * @param isKernel - a flag to indicate Kernel or UST (true for Kernel, false for UST) 
      * @param info - channel information used for creation of a channel (or null for default)
      * @param monitor - a progress monitor 
      * @throws ExecutionException
      */
    public void enableChannels(String sessionName, List<String> channelNames, boolean isKernel, IChannelInfo info, IProgressMonitor monitor) throws ExecutionException;

     /**
      * Disables a list of channels for given session and given channel information (configuration). 
      * @param sessionName - a session name to create
      * @param channelNames - a list of channel names to enable
      * @param isKernel - a flag to indicate Kernel or UST (true for Kernel, false for UST) 
      * @param monitor - a progress monitor 
      * @throws ExecutionException
      */
    public void disableChannels(String sessionName, List<String> channelNames, boolean isKernel, IProgressMonitor monitor) throws ExecutionException;

    /**
     * Enables a list of events with no additional parameters.
     * @param sessionName - a session name
     * @param channelName - a channel name (null for default channel)
     * @param eventNames - a list of event names to enabled.
     * @param isKernel -  a flag for indicating kernel or UST.
     * @param monitor - a progress monitor
     * @throws ExecutionException
     */
    public void enableEvents(String sessionName, String channelName, List<String> eventNames, boolean isKernel, IProgressMonitor monitor) throws ExecutionException;

    /**
     * Enables all syscall events.
     * @param sessionName - a session name
     * @param channelName - a channel name (null for default channel)
     * @param monitor - a progress monitor
     * @throws ExecutionException
     */
    public void enableSyscalls(String sessionName, String channelName, IProgressMonitor monitor) throws ExecutionException;

    /**
     * Enables a dynamic probe.
     * @param sessionName - a session name
     * @param channelName - a channel name (null for default channel)
     * @param monitor - a progress monitor
     * @param eventName - a event name
     * @param probe - a dynamic probe information 
     * @throws ExecutionException
     */
    public void enableProbe(String sessionName, String channelName, String eventName, String probe, IProgressMonitor monitor) throws ExecutionException;

    /**
     * Enables a dynamic function entry/return probe.
     * @param sessionName - a session name
     * @param channelName - a channel name (null for default channel)
     * @param monitor - a progress monitor
     * @param eventName - a event name
     * @param function - a dynamic function entry/return probe information 
     * @throws ExecutionException
     */
    public void enableFunctionProbe(String sessionName, String channelName, String eventName, String probe, IProgressMonitor monitor) throws ExecutionException;

    /**
     * Disables a list of events with no additional parameters.
     * @param sessionName - a session name
     * @param channelName - a channel name (null for default channel)
     * @param eventNames - a list of event names to enabled.
     * @param isKernel -  a flag for indicating kernel or UST.
     * @param monitor - a progress monitor
     * @throws ExecutionException
     */
    public void disableEvent(String sessionName, String channelName, List<String> eventNames, boolean isKernel, IProgressMonitor monitor) throws ExecutionException;

}
