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
package org.eclipse.linuxtools.internal.lttng2.core.control.model.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.linuxtools.internal.lttng2.core.control.model.IChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.IEventInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TraceEnablement;

/**
 * <b><u>ChannelInfo</u></b>
 * <p>
 * Implementation of the trace channel interface (IChannelInfo) to store channel
 * related data. 
 * </p>
 */
public class ChannelInfo extends TraceInfo implements IChannelInfo {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The overwrite mode of the channel.
     */
    private boolean fOverwriteMode;
    /**
     * The sub-buffer size of the channel.
     */
    private long fSubBufferSize;
    /**
     * The number of sub-buffers of the channel.
     */
    private int fNumberOfSubBuffers;
    /**
     * The switch timer interval of the channel.
     */
    private long fSwitchTimer;
    /**
     * The read timer interval of the channel.
     */
    private long fReadTimer;
    /**
     * The Output type of the channel.
     */
    private String fOutputType = ""; //$NON-NLS-1$
    /**
     * The channel enable state.
     */
    private TraceEnablement fState = TraceEnablement.DISABLED;
    /**
     * The events information of the channel.
     */
    private List<IEventInfo> fEvents = new ArrayList<IEventInfo>();

    
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param name - name channel
     */
    public ChannelInfo(String name) {
        super(name);
    }

    /**
     * Copy constructor
     * @param other - the instance to copy
     */
    public ChannelInfo(ChannelInfo other) {
        super(other);
        fOverwriteMode = other.fOverwriteMode;
        fSubBufferSize = other.fSubBufferSize;
        fNumberOfSubBuffers = other.fNumberOfSubBuffers;
        fSwitchTimer = other.fSwitchTimer;
        fReadTimer = other.fReadTimer;
        fOutputType = (other.fOutputType == null ? null : String.valueOf(other.fOutputType));
        fState = other.fState;
        for (Iterator<IEventInfo> iterator = other.fEvents.iterator(); iterator.hasNext();) {
            IEventInfo event = iterator.next();
            if (event instanceof EventInfo) {
                fEvents.add(new EventInfo((EventInfo)event));
            } else {
                fEvents.add(event);
            }
        }
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IChannelInfo#getOverwriteMode()
     */
    @Override
    public boolean isOverwriteMode() {
        return fOverwriteMode;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IChannelInfo#setOverwriteMode(boolean)
     */
    @Override
    public void setOverwriteMode(boolean mode) {
        fOverwriteMode = mode;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IChannelInfo#getSubBufferSize()
     */
    @Override
    public long getSubBufferSize() {
        return fSubBufferSize;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IChannelInfo#setSubBufferSize(long)
     */
    @Override
    public void setSubBufferSize(long bufferSize) {
        fSubBufferSize = bufferSize;

    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IChannelInfo#getNumberOfSubBuffers()
     */
    @Override
    public int getNumberOfSubBuffers() {
        return fNumberOfSubBuffers;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IChannelInfo#setNumberOfSubBuffers(int)
     */
    @Override
    public void setNumberOfSubBuffers(int numberOfSubBuffers) {
        fNumberOfSubBuffers = numberOfSubBuffers;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IChannelInfo#getSwitchTimer()
     */
    @Override
    public long getSwitchTimer() {
        return fSwitchTimer;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IChannelInfo#setSwitchTimer(long)
     */
    @Override
    public void setSwitchTimer(long timer) {
        fSwitchTimer = timer;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IChannelInfo#getReadTimer()
     */
    @Override
    public long getReadTimer() {
        return fReadTimer;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IChannelInfo#setReadTimer(long)
     */
    @Override
    public void setReadTimer(long timer) {
        fReadTimer = timer;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IChannelInfo#getOutputType()
     */
    @Override
    public String getOutputType() {
        return fOutputType;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IChannelInfo#setOutputType(java.lang.String)
     */
    @Override
    public void setOutputType(String type) {
        fOutputType = type;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IChannelInfo#getState()
     */
    @Override
    public TraceEnablement getState() {
        return fState;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IChannelInfo#setState(org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.TraceEnablement)
     */
    @Override
    public void setState(TraceEnablement state) {
        fState = state;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IChannelInfo#setState(java.lang.String)
     */
    @Override
    public void setState(String stateName) {
        fState = TraceEnablement.ENABLED;
        if (TraceEnablement.DISABLED.getInName().equals(stateName)) {
            fState = TraceEnablement.DISABLED;
        } else if (TraceEnablement.ENABLED.getInName().equals(stateName)) {
            fState = TraceEnablement.ENABLED;
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IChannelInfo#getEvents()
     */
    @Override
    public IEventInfo[] getEvents() {
        return fEvents.toArray(new IEventInfo[fEvents.size()]);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IChannelInfo#setEvents(java.util.List)
     */
    @Override
    public void setEvents(List<IEventInfo> events) {
        for (Iterator<IEventInfo> iterator = events.iterator(); iterator.hasNext();) {
            IEventInfo eventInfo = (IEventInfo) iterator.next();
            fEvents.add(eventInfo);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IChannelInfo#addEvent(org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IEventInfo)
     */
    @Override
    public void addEvent(IEventInfo channel) {
        fEvents.add(channel);
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceInfo#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((fEvents == null) ? 0 : fEvents.hashCode());
        result = prime * result + fNumberOfSubBuffers;
        result = prime * result + ((fOutputType == null) ? 0 : fOutputType.hashCode());
        result = prime * result + (fOverwriteMode ? 1231 : 1237);
        result = prime * result + (int) (fReadTimer ^ (fReadTimer >>> 32));
        result = prime * result + ((fState == null) ? 0 : (fState.ordinal() + 1));
        result = prime * result + (int) (fSubBufferSize ^ (fSubBufferSize >>> 32));
        result = prime * result + (int) (fSwitchTimer ^ (fSwitchTimer >>> 32));
        return result;
    }  

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceInfo#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ChannelInfo other = (ChannelInfo) obj;
        if (fEvents == null) {
            if (other.fEvents != null) {
                return false;
            }
        } else if (!fEvents.equals(other.fEvents)) {
            return false;
        }
        if (fNumberOfSubBuffers != other.fNumberOfSubBuffers) {
            return false;
        }
        if (fOutputType == null) {
            if (other.fOutputType != null) {
                return false;
            }
        } else if (!fOutputType.equals(other.fOutputType)) {
            return false;
        }
        if (fOverwriteMode != other.fOverwriteMode) {
            return false;
        }
        if (fReadTimer != other.fReadTimer) {
            return false;
        }
        if (fState != other.fState) {
            return false;
        }
        if (fSubBufferSize != other.fSubBufferSize) {
            return false;
        }
        if (fSwitchTimer != other.fSwitchTimer) {
            return false;
        }
        return true;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceInfo#toString()
     */
    @SuppressWarnings("nls")
    @Override
    public String toString() {
        StringBuffer output = new StringBuffer();
            output.append("[ChannelInfo(");
            output.append(super.toString());
            output.append(",State=");
            output.append(fState);
            output.append(",OverwriteMode=");
            output.append(fOverwriteMode);
            output.append(",SubBuffersSize=");
            output.append(fSubBufferSize);
            output.append(",NumberOfSubBuffers=");
            output.append(fNumberOfSubBuffers);
            output.append(",SwitchTimer=");
            output.append(fSwitchTimer);
            output.append(",ReadTimer=");
            output.append(fReadTimer);
            output.append(",output=");
            output.append(fOutputType);
            output.append(",Events=");
            if (fEvents.isEmpty()) {
                output.append("None");
            } else {
                for (Iterator<IEventInfo> iterator = fEvents.iterator(); iterator.hasNext();) {
                    IEventInfo event = (IEventInfo) iterator.next();
                    output.append(event.toString());
                }
            }
            output.append(")]");
            return output.toString();
    }


}