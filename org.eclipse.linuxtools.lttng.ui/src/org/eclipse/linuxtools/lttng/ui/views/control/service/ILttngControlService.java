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
     * @return an array with session names.
     * @throws ExecutionException
     */
    public String[] getSessionNames() throws ExecutionException;
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
     * @return session information
     * @throws ExecutionException
     */
    public ISessionInfo getSession(String sessionName) throws ExecutionException;
    
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
     * @return the list of existing kernel events.
     * @throws ExecutionException
     */
    public List<IBaseEventInfo> getKernelProvider() throws ExecutionException;
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
     * @return - the UST provider information.
     * @throws ExecutionException
     */
    public List<IUstProviderInfo> getUstProvider(IProgressMonitor monitor) throws ExecutionException;
}
