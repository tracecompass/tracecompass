/**********************************************************************
 * Copyright (c) 2012, 2013 Ericsson
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

import java.util.List;

/**
 * <p>
 * Interface for retrieval of UST provider information.
 * </p>
 *
 * @author Bernd Hufmann
 */
public interface IUstProviderInfo extends ITraceInfo {

    /**
     * @return the process ID of the UST provider.
     */
    int getPid();

    /**
     * Sets the process ID of the UST provider to the given value.
     * @param pid - process ID to set
     */
    void setPid(int pid);

    /**
     * @return all event information as array.
     */
    IBaseEventInfo[] getEvents();

    /**
     * Sets the event information specified by given list.
     * @param events - all event information to set.
     */
    void setEvents(List<IBaseEventInfo> events);

    /**
     * Adds a single event information.
     * @param event - event information to add.
     */
    void addEvent(IBaseEventInfo event);
}
