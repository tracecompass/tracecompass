/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.event;

import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * <b><u>ITmfDataEvent</u></b>
 * <p>
 * The basic event structure in the TMF. In its canonical form, an event has:
 * <ul>
 * <li> a parent trace
 * <li> a rank (order within the trace)
 * <li> a source (reporting component)
 * <li> a type
 * <li> a content
 * </ul>
 * For convenience, a free-form reference field is also provided. It could be
 * used as e.g. a location marker (filename:lineno) to indicate where the event
 * was generated.
 */
public interface ITmfDataEvent extends Cloneable {

    /**
     * @return the trace that 'owns' the event
     */
    public ITmfTrace<?> getTrace();

    /**
     * @return the event rank within the parent trace
     */
    public long getRank();

    /**
     * @return the event source
     */
    public String getSource();

    /**
     * @return the event type
     */
    public ITmfEventType getType();

    /**
     * @return the event content
     */
    public ITmfEventContent getContent();

    /**
     * @return the event reference
     */
    public String getReference();

    /**
     * @return a clone of the data event
     */
    public ITmfDataEvent clone();
    
}
