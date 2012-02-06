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
package org.eclipse.linuxtools.lttng.ui.views.control.model;

import java.util.List;

/**
 * <b><u>ISessionInfo</u></b>
 * <p>
 * Interface for retrieval of trace session information.
 * </p>
 */
public interface ISessionInfo extends ITraceInfo {

    /**
     * @return the session state state (active or inactive).
     */
    public TraceSessionState getSessionState();
    /**
     * Sets the session state  to the given value.
     * @param state - state to set.
     */
    public void setSessionState(TraceSessionState state);
    
    /**
     * Sets the event state to the value specified by the given name.
     * @param stateName - state to set.
     */
    public void setSessionState(String stateName);
    
    /**
     * @return path string where session is located.
     */
    public String getSessionPath();
    
    /**
     * Sets the path string (where session is located) to the given value.
     * @param path - session path to set.
     */
    public void setSessionPath(String path);
    
    /**
     * @return all domain information as array.
     */
    public IDomainInfo[] getDomains();
    
    /**
     * Sets all domain information specified by given list.
     * @param domains - all domain information to set.
     */
    public void setDomains(List<IDomainInfo> domains);
    
    /**
     * Adds a single domain information.
     * @param domain - domain information to add.
     */
    public void addDomain(IDomainInfo domainInfo);
}
