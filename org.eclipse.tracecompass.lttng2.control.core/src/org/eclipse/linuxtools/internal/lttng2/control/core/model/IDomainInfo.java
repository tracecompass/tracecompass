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
package org.eclipse.linuxtools.internal.lttng2.control.core.model;

import java.util.List;

import org.eclipse.linuxtools.internal.lttng2.control.core.model.impl.BufferType;

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
     * Sets the channel information specified by given list.
     * @param channels - all channel information to set.
     */
    void setChannels(List<IChannelInfo> channels);

    /**
     * Adds a single channel information.
     * @param channel - channel information to add.
     */
    void addChannel(IChannelInfo channel);

    /**
     * @return true if domain is kernel, false for UST
     */
    boolean isKernel();

    /**
     * Sets whether domain is  Kernel domain or UST
     * @param isKernel true for kernel, false for UST
     */
    void setIsKernel(boolean isKernel);

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
