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

import org.eclipse.linuxtools.lttng.ui.LTTngUiPlugin;
import org.eclipse.linuxtools.lttng.ui.views.control.Messages;
import org.eclipse.linuxtools.lttng.ui.views.control.model.IChannelInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.IEventInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.ITraceControlComponent;
import org.eclipse.linuxtools.lttng.ui.views.control.model.TraceEnablement;
import org.eclipse.linuxtools.lttng.ui.views.control.property.TraceChannelPropertySource;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertySource;


/**
 * <b><u>TraceChannelComponent</u></b>
 * <p>
 * Implementation of the trace channel component.
 * </p>
 */
public class TraceChannelComponent extends TraceControlComponent {
    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * Path to icon file for this component (state enabled).
     */
    public static final String TRACE_CHANNEL_ICON_FILE_ENABLED = "icons/obj16/channel.gif"; //$NON-NLS-1$
    /**
     * Path to icon file for this component (state disabled).
     */
    public static final String TRACE_CHANNEL_ICON_FILE_DISABLED = "icons/obj16/channel_disabled.gif"; //$NON-NLS-1$
    
    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The channel information.
     */
    private IChannelInfo fChannelInfo = null;
    /**
     * The image to be displayed in disabled state.
     */
    private Image fDisabledImage = null;
    
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor 
     * @param name - the name of the component.
     * @param parent - the parent of this component.
     */
    public TraceChannelComponent(String name, ITraceControlComponent parent) {
        super(name, parent);
        setImage(TRACE_CHANNEL_ICON_FILE_ENABLED);
        setToolTip(Messages.TraceControl_ChannelDisplayName);
        fChannelInfo = new ChannelInfo(name);
        fDisabledImage = LTTngUiPlugin.getDefault().loadIcon(TRACE_CHANNEL_ICON_FILE_DISABLED);
    }
    
    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TraceControlComponent#getImage()
     */
    @Override
    public Image getImage() {
        if (fChannelInfo.getState() == TraceEnablement.DISABLED) {
            return fDisabledImage;
        }
        return super.getImage();
    }

    /**
     * Sets the channel information.
     * @param channelInfo
     */
    public void setChannelInfo(IChannelInfo channelInfo) {
        fChannelInfo = channelInfo;
        IEventInfo[] events = fChannelInfo.getEvents();
        for (int i = 0; i < events.length; i++) {
            TraceEventComponent event = new TraceEventComponent(events[i].getName(), this);
            event.setEventInfo(events[i]);
            addChild(event);
        }
    }

    /**
     * @return the overwrite mode value.
     */
    public boolean isOverwriteMode() {
        return fChannelInfo.isOverwriteMode();
    }
    /**
     * Sets the overwrite mode value to the given mode.
     * @param mode - mode to set.
     */
    public void setOverwriteMode(boolean mode){
        fChannelInfo.setOverwriteMode(mode);
    }
    /**
     * @return the sub-buffer size.
     */
    public long getSubBufferSize() {
        return fChannelInfo.getSubBufferSize();
    }
    /**
     * Sets the sub-buffer size to the given value.
     * @param bufferSize - size to set to set.
     */
    public void setSubBufferSize(long bufferSize) {
        fChannelInfo.setSubBufferSize(bufferSize);
    }
    /**
     * @return the number of sub-buffers.
     */
    public int getNumberOfSubBuffers() {
        return fChannelInfo.getNumberOfSubBuffers();
    }
    /**
     * Sets the number of sub-buffers to the given value.
     * @param numberOfSubBuffers - value to set.
     */
    public void setNumberOfSubBuffers(int numberOfSubBuffers) {
        fChannelInfo.setNumberOfSubBuffers(numberOfSubBuffers);
    }
    /**
     * @return the switch timer interval.
     */
    public long getSwitchTimer() {
        return fChannelInfo.getSwitchTimer();
    }
    /**
     * Sets the switch timer interval to the given value.
     * @param timer - timer value to set.
     */
    public void setSwitchTimer(long timer) {
        fChannelInfo.setSwitchTimer(timer);
    }
    /**
     * @return the read timer interval.
     */
    public long getReadTimer() {
        return fChannelInfo.getReadTimer(); 
    }
    /**
     * Sets the read timer interval to the given value.
     * @param timer - timer value to set..
     */
    public void setReadTimer(long timer) {
        fChannelInfo.setReadTimer(timer);
    }
    /**
     * @return the output type.
     */
    public String getOutputType() {
        return fChannelInfo.getOutputType();
    }
    /**
     * Sets the output type to the given value.
     * @param type - type to set.
     */
    public void setOutputType(String type) {
        fChannelInfo.setOutputType(type);
    }
    /**
     * @return the channel state (enabled or disabled).
     */
    public TraceEnablement getState() {
        return fChannelInfo.getState();
    }
    /**
     * Sets the channel state (enablement) to the given value.
     * @param state - state to set.
     */
    public void setState(TraceEnablement state) {
        fChannelInfo.setState(state);
    }
    /**
     * Sets the channel state (enablement) to the value specified by the given name.
     * @param stateName - state to set.
     */
    public void setState(String stateName) {
        fChannelInfo.setState(stateName);
    }
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TraceControlComponent#getAdapter(java.lang.Class)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == IPropertySource.class) {
            return new TraceChannelPropertySource(this);
        }
        return null;
    } 
    
    
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
}
