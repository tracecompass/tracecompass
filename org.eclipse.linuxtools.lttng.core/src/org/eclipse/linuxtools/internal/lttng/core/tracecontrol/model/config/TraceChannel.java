/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.config;

import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.config.TraceChannel;

/**
 * <b><u>TraceChannel</u></b>
 * <p>
 *  This models a trace channel representing a channel on a particular remote system.
 * </p>
 */
public class TraceChannel implements Cloneable {
    
    // ------------------------------------------------------------------------
    // Attributes
    // -----------------------------------------------------------------------

    public static final int UNKNOWN_VALUE = -1;
    public static final String UNKNOWN_STRING = "?";  //$NON-NLS-1$
    public static final String UST_TRACE_CHANNEL_NAME = "AUTO"; //$NON-NLS-1$
    
    private String  fName = ""; //$NON-NLS-1$
    private boolean fIsEnabled = true;
    private boolean fIsEnabledStatusKnown = false;
    private boolean fIsChannelOverride = false;
    private boolean fIsChannelOverrideStatusKnown = false;
    private long    fSubbufNum = 0;
    private long    fSubbufSize = 0;
    private long    fTimer = 0;
    
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *  
     * @param name The name of the channel
     * @param subbufNum The number of sub-buffers
     * @param subbufSize The size of the sub-buffers
     * @param timer The Channel timer
     */
    public TraceChannel(String name, long subbufNum, long subbufSize, long timer) {
        fName = name;
        fIsEnabled = false;
        fIsEnabledStatusKnown = false;
        fIsChannelOverride = false;
        fIsChannelOverrideStatusKnown = false;
        fSubbufNum = subbufNum;
        fSubbufSize = subbufSize;
        fTimer = timer;
    }
    
    /**
     * Constructor
     * 
     * @param name The name of the channel
     * @param isEnabled The state of the channel (enabled or disabled)
     * @param issChannelOverride The state of the channel override (enabled or disabled)
     * @param subbufNum The number of sub-buffers
     * @param subbufSize The size of the sub-buffers
     * @param timer The Channel timer
     */
    public TraceChannel(String name, boolean isEnabled, boolean issChannelOverride, long subbufNum, long subbufSize, long timer) {
        fName = name;
        fIsEnabled = isEnabled;
        fIsEnabledStatusKnown = true;
        fIsChannelOverride = issChannelOverride;
        fIsChannelOverrideStatusKnown = true;
        fSubbufNum = subbufNum;
        fSubbufSize = subbufSize;
        fTimer = timer;
    }       
    
    public TraceChannel (String name) {
        this(name, UNKNOWN_VALUE, UNKNOWN_VALUE, UNKNOWN_VALUE);
    }
    
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    
    /**
     * Gets the name of the channel.
     * 
     * @return name of channel.
     */
    public String getName () {
        return fName;
    }
    
    /**
     * Sets the name of the channel.
     * 
     * @param name The name of channel to set
     */
    public void setName(String name) {
        fName = name;
    }
    
    /**
     * Returns whether the channel is enabled or not
     * 
     * @return true if enabled, false if disabled
     */
    public boolean isEnabled() {
        return fIsEnabled;
    }

    /**
     * Sets the state of the channel.
     * 
     * @param isEnabled
     */
    public void setIsEnabled(boolean isEnabled) {
        fIsEnabled = isEnabled;
        fIsEnabledStatusKnown = true;
    }

    /**
     * Returns a flag to indicate whether the enabled state on the remote system is known or not.
     * 
     * @return true if known else false
     */
    public boolean isEnabledStatusKnown() {
        return fIsEnabledStatusKnown;
    }

    /**
     * Sets a flag to indicate whether the enabled state on the remote system is known or not.
     * @param isKnown
     */
    public void setIsEnabledStatusKnown(boolean isKnown) {
        fIsEnabledStatusKnown = isKnown;
    }

    /**
     * Returns whether the channel buffer overwrite is enabled or not
     * 
     * @return true if enabled, false if disabled
     */

    public boolean isChannelOverride() {
        return fIsChannelOverride;
    }

    /**
     * Sets the state of the channel buffer overwrite.
     * @param isChannelOverride
     */
    public void setIsChannelOverride(boolean isChannelOverride) {
        this.fIsChannelOverride = isChannelOverride;
        this.fIsChannelOverrideStatusKnown = true;
    }
    /**
     * Returns a flag to indicate whether the channel overwrite state on the remote system is known or not.
     * 
     * @return true if known else false
     */
    public boolean isChannelOverrideStatusKnown() {
        return fIsChannelOverrideStatusKnown;
    }
    
    /**
     * Sets a flag to indicate whether the channel overwrite state on the remote system is known or not.
     * 
     * @param isKnown
     */
    public void setIsChannelOverrideStatusKnown(boolean isKnown) {
        this.fIsChannelOverrideStatusKnown = isKnown;
    }

    /**
     * Gets the number of sub-buffers.
     * 
     * @return subBufNum
     */
    public long getSubbufNum() {
        return fSubbufNum;
    }

    /**
     * Sets the number of sub-buffers.
     * @param subbufNum
     */
    public void setSubbufNum(long subbufNum) {
        this.fSubbufNum = subbufNum;
    }

    /**
     * Returns the size of the sub-buffers.
     * 
     * @return subbufSize
     */
    public long getSubbufSize() {
        return fSubbufSize;
    }

    /**
     * Sets the size of the sub-buffers.
     * 
     * @param subbufSize
     */
    public void setSubbufSize(long subbufSize) {
        this.fSubbufSize = subbufSize;
    }

    /** 
     * Returns the channel timer.
     * 
     * @return channel timer
     */
    public long getTimer() {
        return fTimer;
    }

    /**
     * Sets the channel timer.
     * 
     * @param timer
     */
    public void setTimer(long timer) {
        this.fTimer = timer;
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public TraceChannel clone() {
        TraceChannel clone = null;
        try {
            clone = (TraceChannel)super.clone();
            clone.fName = fName;
            clone.fIsEnabled = fIsEnabled;
            clone.fIsEnabledStatusKnown = fIsEnabledStatusKnown;
            clone.fIsChannelOverride = fIsChannelOverride;
            clone.fIsChannelOverrideStatusKnown = fIsChannelOverrideStatusKnown;
            clone.fSubbufNum = fSubbufNum;
            clone.fSubbufSize = fSubbufSize;
            clone.fTimer = fTimer;            
        } catch (CloneNotSupportedException e) {
        }
        return clone;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override 
    public boolean equals(Object other) {
        
        if (this == other) {
            return true;
        }

        if (other == null) {
            return false;
        }

        if (!(other instanceof TraceChannel)) {
            return false;
        }

        TraceChannel otherChannel = (TraceChannel) other;

        if (!otherChannel.fName.equals(fName)) {
            return false;
        }
        if (otherChannel.fIsEnabled != fIsEnabled) {
            return false;
        }
        if (otherChannel.fIsEnabledStatusKnown != fIsEnabledStatusKnown) {
            return false;
        }
        if (otherChannel.fIsChannelOverride != fIsChannelOverride) {
            return false;
        }
        if (otherChannel.fIsChannelOverrideStatusKnown != fIsChannelOverrideStatusKnown) { 
            return false;
        }
        if (otherChannel.fSubbufNum != fSubbufNum) {
            return false;
        }
        if (otherChannel.fSubbufSize != fSubbufSize) {
            return false;
        }
        if (otherChannel.fTimer != fTimer) {
            return false;
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override 
    public int hashCode() {
        // slow algorithm
        StringBuffer builder = new StringBuffer(fName);
        builder.append(fIsEnabled);
        builder.append(fIsEnabledStatusKnown);
        builder.append(fIsChannelOverride);
        builder.append(fIsChannelOverrideStatusKnown);
        builder.append(fSubbufNum);
        builder.append(fSubbufSize);
        builder.append(fTimer);
        return builder.toString().hashCode();
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "[TraceChannel (" + fName + ")]";
    }

    
    
}
