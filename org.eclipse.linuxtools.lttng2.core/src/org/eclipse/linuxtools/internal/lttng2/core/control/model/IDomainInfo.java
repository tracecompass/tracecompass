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

import java.util.List;

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
    public IChannelInfo[] getChannels();
    
    /**
     * Sets the channel information specified by given list.
     * @param channels - all channel information to set.
     */
    public void setChannels(List<IChannelInfo> channels);
    
    /**
     * Adds a single channel information.
     * @param channel - channel information to add.
     */
    public void addChannel(IChannelInfo channel);
    
    /**
     * @return true if domain is kernel, false for UST
     */
    public boolean isKernel();
    
    /**
     * Sets whether domain is  Kernel domain or UST 
     * @param isKernel true for kernel, false for UST
     */
    public void setIsKernel(boolean isKernel);


}
