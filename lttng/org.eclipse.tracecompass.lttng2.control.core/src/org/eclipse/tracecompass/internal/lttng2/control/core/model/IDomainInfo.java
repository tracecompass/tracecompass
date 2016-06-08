/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.core.model;

import java.util.List;

import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.BufferType;

/**
 * <p>
 * Interface for retrieval of trace domain information.
 * </p>
 *
 * @author Bernd Hufmann
 */
public interface IDomainInfo extends ITraceInfo {

    /**
     * @return information about all channels
     */
    IChannelInfo[] getChannels();

    /**
     * @return information about all loggers
     */
    List<ILoggerInfo> getLoggers();

    /**
     * Sets the channel information specified by given list.
     * @param channels - all channel information to set.
     */
    void setChannels(List<IChannelInfo> channels);

    /**
     * Sets the logger information specified by given list.
     * @param loggers - all loggers information to set.
     */
    void setLoggers(List<ILoggerInfo> loggers);


    /**
     * Adds a single channel information.
     * @param channel - channel information to add.
     */
    void addChannel(IChannelInfo channel);

    /**
     * @return the domain type ({@link TraceDomainType})
     */
    TraceDomainType getDomain();

    /**
     * Sets the domain type.
     * @param domain - the domain type ({@link TraceDomainType})
     */
    void setDomain(TraceDomainType domain);

    /**
     * @return Information about the buffer type
     */
    BufferType getBufferType();

    /**
     * Sets the buffer type
     *
     * @param bufferType
     *            The buffer type
     */
    void setBufferType(BufferType bufferType);


}
