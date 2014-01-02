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
 *   Simon Delisle - Updated for support of LTTng Tools 2.2
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.core.control.model.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.linuxtools.internal.lttng2.core.control.model.IChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.IEventInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TraceEnablement;

/**
 * <p>
 * Implementation of the trace channel interface (IChannelInfo) to store channel
 * related data.
 * </p>
 *
 * @author Bernd Hufmann
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
    private final List<IEventInfo> fEvents = new ArrayList<>();
    /**
     * The maximum size of trace files
     */
    private int fMaxSizeTraceFiles;
    /**
     * The maximum number of trace files
     */
    private int fMaxNumberTraceFiles;
    /**
     * The value of buffer type
     */
    private BufferType fBufferType = BufferType.BUFFER_TYPE_UNKNOWN;


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
        fMaxSizeTraceFiles = other.fMaxSizeTraceFiles;
        fMaxNumberTraceFiles = other.fMaxNumberTraceFiles;
        fBufferType = other.fBufferType;
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

    @Override
    public boolean isOverwriteMode() {
        return fOverwriteMode;
    }

    @Override
    public void setOverwriteMode(boolean mode) {
        fOverwriteMode = mode;
    }

    @Override
    public long getSubBufferSize() {
        return fSubBufferSize;
    }

    @Override
    public void setSubBufferSize(long bufferSize) {
        fSubBufferSize = bufferSize;
    }

    @Override
    public int getNumberOfSubBuffers() {
        return fNumberOfSubBuffers;
    }

    @Override
    public void setNumberOfSubBuffers(int numberOfSubBuffers) {
        fNumberOfSubBuffers = numberOfSubBuffers;
    }

    @Override
    public long getSwitchTimer() {
        return fSwitchTimer;
    }

    @Override
    public void setSwitchTimer(long timer) {
        fSwitchTimer = timer;
    }

    @Override
    public long getReadTimer() {
        return fReadTimer;
    }

    @Override
    public void setReadTimer(long timer) {
        fReadTimer = timer;
    }

    @Override
    public String getOutputType() {
        return fOutputType;
    }

    @Override
    public void setOutputType(String type) {
        fOutputType = type;
    }

    @Override
    public TraceEnablement getState() {
        return fState;
    }

    @Override
    public void setState(TraceEnablement state) {
        fState = state;
    }

    @Override
    public void setState(String stateName) {
        fState = TraceEnablement.ENABLED;
        if (TraceEnablement.DISABLED.getInName().equals(stateName)) {
            fState = TraceEnablement.DISABLED;
        } else if (TraceEnablement.ENABLED.getInName().equals(stateName)) {
            fState = TraceEnablement.ENABLED;
        }
    }

    @Override
    public IEventInfo[] getEvents() {
        return fEvents.toArray(new IEventInfo[fEvents.size()]);
    }

    @Override
    public void setEvents(List<IEventInfo> events) {
        fEvents.clear();
        for (Iterator<IEventInfo> iterator = events.iterator(); iterator.hasNext();) {
            IEventInfo eventInfo = iterator.next();
            fEvents.add(eventInfo);
        }
    }

    @Override
    public void addEvent(IEventInfo channel) {
        fEvents.add(channel);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + fEvents.hashCode();
        result = prime * result + fNumberOfSubBuffers;
        result = prime * result + ((fOutputType == null) ? 0 : fOutputType.hashCode());
        result = prime * result + (fOverwriteMode ? 1231 : 1237);
        result = prime * result + (int) (fReadTimer ^ (fReadTimer >>> 32));
        result = prime * result + ((fState == null) ? 0 : (fState.ordinal() + 1));
        result = prime * result + (int) (fSubBufferSize ^ (fSubBufferSize >>> 32));
        result = prime * result + (int) (fSwitchTimer ^ (fSwitchTimer >>> 32));
        result = prime * result + ((fBufferType == null) ? 0 : (fBufferType.ordinal() + 1));
        return result;
    }

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
        if (!fEvents.equals(other.fEvents)) {
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
        if (fBufferType != other.fBufferType) {
            return false;
        }
        return true;
    }

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
            if ((fBufferType != null) && !fBufferType.equals(BufferType.BUFFER_TYPE_UNKNOWN) && !fBufferType.equals(BufferType.BUFFER_SHARED)) {
                output.append(",BufferType=");
                output.append(fBufferType);
            }
            output.append(",Events=");
            if (fEvents.isEmpty()) {
                output.append("None");
            } else {
                for (Iterator<IEventInfo> iterator = fEvents.iterator(); iterator.hasNext();) {
                    IEventInfo event = iterator.next();
                    output.append(event.toString());
                }
            }
            output.append(")]");
            return output.toString();
    }

    @Override
    public void setMaxSizeTraceFiles(int maxSizeTraceFiles) {
        fMaxSizeTraceFiles = maxSizeTraceFiles;
    }

    @Override
    public void setMaxNumberTraceFiles(int maxNumberTraceFiles) {
        fMaxNumberTraceFiles = maxNumberTraceFiles;
    }

    @Override
    public int getMaxSizeTraceFiles() {
        return fMaxSizeTraceFiles;
    }

    @Override
    public int getMaxNumberTraceFiles() {
        return fMaxNumberTraceFiles;
    }

    @Override
    public void setBufferType(BufferType bufferType) {
        fBufferType = bufferType;
    }

    @Override
    public BufferType getBufferType() {
        return fBufferType;
    }
}
