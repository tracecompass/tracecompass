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
 * <b><u>IChannelInfo</u></b>
 * <p>
 * Interface for retrieval of trace channel information.
 * </p>
 */
public interface IChannelInfo extends ITraceInfo {

    /**
     * @return the overwrite mode value.
     */
    public boolean isOverwriteMode();
    /**
     * Sets the overwrite mode value to the given mode.
     * @param mode - mode to set.
     */
    public void setOverwriteMode(boolean mode);

    /**
     * @return the sub-buffer size.
     */
    public long getSubBufferSize();
    /**
     * Sets the sub-buffer size to the given value.
     * @param bufferSize - size to set to set.
     */
    public void setSubBufferSize(long bufferSize);

    /**
     * @return the number of sub-buffers.
     */
    public int getNumberOfSubBuffers();
    /**
     * Sets the number of sub-buffers to the given value.
     * @param numberOfSubBuffers - value to set.
     */
    public void setNumberOfSubBuffers(int numberOfSubBuffers);

    /**
     * @return the switch timer interval.
     */
    public long getSwitchTimer();
    /**
     * Sets the switch timer interval to the given value.
     * @param timer - timer value to set.
     */
    public void setSwitchTimer(long timer);
    
    /**
     * @return the read timer interval.
     */
    public long getReadTimer();
    /**
     * Sets the read timer interval to the given value.
     * @param timer - timer value to set..
     */
    public void setReadTimer(long timer);

    /**
     * @return the output type.
     */
    public String getOutputType();
    /**
     * Sets the output type to the given value.
     * @param type - type to set.
     */
    public void setOutputType(String type);
    
    /**
     * @return the channel state (enabled or disabled).
     */
    public TraceEnablement getState();
    /**
     * Sets the channel state (enablement) to the given value.
     * @param state - state to set.
     */
    public void setState(TraceEnablement state);
    /**
     * Sets the channel state (enablement) to the value specified by the given name.
     * @param stateName - state to set.
     */
    public void setState(String stateName);

    /**
     * @return all event information as array.
     */
    public IEventInfo[] getEvents();
    /**
     * Sets the event information specified by given list.
     * @param events - all event information to set.
     */
    public void setEvents(List<IEventInfo> events);
    /**
     * Adds a single event information.
     * @param event - event information to add.
     */
    public void addEvent(IEventInfo event);
}
