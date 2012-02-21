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
package org.eclipse.linuxtools.lttng.ui.views.control.model.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.linuxtools.lttng.ui.views.control.model.IChannelInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.IDomainInfo;

/**
 * <b><u>DomainInfo</u></b>
 * <p>
 * Implementation of the trace domain interface (IDomainInfo) to store domain
 * related data. 
 * </p>
 */
public class DomainInfo extends TraceInfo implements IDomainInfo {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The channels information of the domain.
     */
    private List<IChannelInfo> fChannels = new ArrayList<IChannelInfo>();
    private boolean fIsKernel = false;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param name - name of domain
     */
    public DomainInfo(String name) {
        super(name);
    }

    /**
     * Copy constructor
     * @param other - the instance to copy
     */
    public DomainInfo(DomainInfo other) {
        super(other);
        for (int i = 0; i < other.fChannels.size(); i++) {
            if (other.fChannels.get(i) instanceof ChannelInfo) {
                fChannels.add(new ChannelInfo((ChannelInfo)other.fChannels.get(i)));
            } else {
                fChannels.add(other.fChannels.get(i));
            }
        }
        fIsKernel = other.fIsKernel;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.model.IDomainInfo#isKernel()
     */
    @Override
    public boolean isKernel() {
        return fIsKernel;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.model.IDomainInfo#setIsKernel(boolean)
     */
    @Override
    public void setIsKernel(boolean isKernel) {
        fIsKernel = isKernel;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.model.IDomainInfo#getChannels()
     */
    @Override
    public IChannelInfo[] getChannels() {
        return fChannels.toArray(new IChannelInfo[fChannels.size()]);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.model.IDomainInfo#setChannels(java.util.List)
     */
    @Override
    public void setChannels(List<IChannelInfo> channels) {
        for (Iterator<IChannelInfo> iterator = channels.iterator(); iterator.hasNext();) {
            IChannelInfo channelInfo = (IChannelInfo) iterator.next();
            fChannels.add(channelInfo);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.model.IDomainInfo#addChannel(org.eclipse.linuxtools.lttng.ui.views.control.model.IChannelInfo)
     */
    @Override
    public void addChannel(IChannelInfo channel) {
        fChannels.add(channel);
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TraceInfo#hashCode()
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + super.hashCode();
        for (Iterator<IChannelInfo> iterator = fChannels.iterator(); iterator.hasNext();) {
            IChannelInfo channel = (IChannelInfo) iterator.next();
            result = 37 * result + channel.hashCode();
        }
        result += 37 * result + (fIsKernel ? 1 : 0);
        return result;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TraceInfo#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof DomainInfo)) {
            return false;
        }

        DomainInfo otherInfo = (DomainInfo) other;
        if (!super.equals(otherInfo)) {
            return false;
        }

        if (fChannels.size() != otherInfo.fChannels.size()) {
            return false;
        }
        for (int i = 0; i < fChannels.size(); i++) {
            if (!fChannels.get(i).equals(otherInfo.fChannels.get(i))) {
                return false;
            }
        }

        if (fIsKernel != otherInfo.fIsKernel) {
            return false;
        }
        return true;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TraceInfo#toString()
     */
    @SuppressWarnings("nls")
    @Override
    public String toString() {
        StringBuffer output = new StringBuffer();
            output.append("[DomainInfo(");
            output.append(super.toString());
            output.append(",Channels=");
            if (fChannels.isEmpty()) {
                output.append("None");
            } else {
                for (Iterator<IChannelInfo> iterator = fChannels.iterator(); iterator.hasNext();) {
                    IChannelInfo channel = (IChannelInfo) iterator.next();
                    output.append(channel.toString());
                }
            }
            output.append(",isKernel=");
            output.append(String.valueOf(fIsKernel));
            output.append(")]");
            return output.toString();
    }
}
