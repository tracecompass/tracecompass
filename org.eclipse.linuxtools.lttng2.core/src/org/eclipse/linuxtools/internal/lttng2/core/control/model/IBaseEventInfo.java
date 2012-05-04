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
 * <b><u>IBaseEventInfo</u></b>
 * <p>
 * Interface for retrieval of basic trace event information.
 * </p>
 */
public interface IBaseEventInfo extends ITraceInfo {

    /**
     * @return the trace event type
     */
    public TraceEventType getEventType();
    
    /**
     * Sets the trace event type to the given type 
     * @param type - type to set
     */
    public void setEventType(TraceEventType type);
    
    /**
     * Sets the trace event type to the type specified by the given name.
     * @param typeName - event type name
     */
    public void setEventType(String typeName);
    
    /**
     * @return the trace event log level
     */
    public TraceLogLevel getLogLevel();
    
    /**
     * Sets the trace event log level to the given level 
     * @param level - event log level to set
     */
    public void setLogLevel(TraceLogLevel level);
    
    /**
     * Sets the trace event log level to the level specified by the given name.
     * @param levelName - event log level name
     */
    public void setLogLevel(String levelName);

    
}
