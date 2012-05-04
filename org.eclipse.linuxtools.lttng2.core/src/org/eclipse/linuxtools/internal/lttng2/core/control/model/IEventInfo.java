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
package org.eclipse.linuxtools.internal.lttng2.core.control.model;

/**
 * <p>
 * Interface for retrieval of trace event information.
 * </p>
 * 
 * @author Bernd Hufmann
 */
public interface IEventInfo extends IBaseEventInfo {

    /**
     * @return the event state (enabled or disabled).
     */
    public TraceEnablement getState();
    
    /**
     * Sets the event state (enablement) to the given value.
     * @param state - state to set.
     */
    public void setState(TraceEnablement state);
    
    /**
     * Sets the event state (enablement) to the value specified by the given name.
     * @param stateName - state to set.
     */
    public void setState(String stateName);
    
}
