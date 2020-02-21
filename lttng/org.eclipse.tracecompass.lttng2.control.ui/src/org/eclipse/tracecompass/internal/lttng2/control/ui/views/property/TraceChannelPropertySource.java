/**********************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.property;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceChannelComponent;
import org.eclipse.tracecompass.tmf.ui.properties.ReadOnlyTextPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

/**
 * <p>
 * Property source implementation for the trace channel component.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class TraceChannelPropertySource extends BasePropertySource {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The trace channel 'name' property ID.
     */
    public static final String TRACE_CHANNEL_NAME_PROPERTY_ID = "trace.channel.name"; //$NON-NLS-1$
    /**
     * The trace channel 'state' ID.
     */
    public static final String TRACE_CHANNEL_STATE_PROPERTY_ID = "trace.channel.state"; //$NON-NLS-1$
    /**
     * The trace channel 'overwrite mode' property ID.
     */
    public static final String TRACE_CHANNEL_OVERWRITE_MODE_PROPERTY_ID = "trace.channel.overwrite.mode"; //$NON-NLS-1$
    /**
     * The trace channel 'sub-buffer size' property ID.
     */
    public static final String TRACE_CHANNEL_SUBBUFFER_SIZE_PROPERTY_ID = "trace.channel.subbuffer.size"; //$NON-NLS-1$
    /**
     * The trace channel 'number of sub-buffers' property ID.
     */
    public static final String TRACE_CHANNEL_NO_SUBBUFFERS_PROPERTY_ID = "trace.channel.no.subbuffers"; //$NON-NLS-1$
    /**
     * The trace channel 'switch timer interval' property ID.
     */
    public static final String TRACE_CHANNEL_SWITCH_TIMER_PROPERTY_ID = "trace.channel.switch.timer"; //$NON-NLS-1$
    /**
     * The trace channel 'read timer interval' property ID.
     */
    public static final String TRACE_CHANNEL_READ_TIMER_PROPERTY_ID = "trace.channel.read.timer"; //$NON-NLS-1$
    /**
     * The trace channel 'output type' property ID.
     */
    public static final String TRACE_CHANNEL_OUTPUT_TYPE_PROPERTY_ID = "trace.channel.output.type"; //$NON-NLS-1$
    /**
     * The trace channel 'trace file count' property ID.
     */
    public static final String TRACE_CHANNEL_TRACE_FILE_COUNT_PROPERTY_ID = "trace.channel.trace.file.count"; //$NON-NLS-1$
    /**
     * The trace channel 'trace file size' property ID.
     */
    public static final String TRACE_CHANNEL_TRACE_FILE_SIZE_PROPERTY_ID = "trace.channel.trace.file.size"; //$NON-NLS-1$
    /**
     * The trace channel 'discarded events' property ID.
     */
    public static final String TRACE_CHANNEL_DISCARDED_EVENTS_PROPERTY_ID = "trace.channel.discarded.events"; //$NON-NLS-1$
    /**
     * The trace channel 'lost packets' property ID.
     */
    public static final String TRACE_CHANNEL_LOST_PACKETS_PROPERTY_ID = "trace.channel.lost.packets"; //$NON-NLS-1$
    /**
     *  The trace channel 'name' property name.
     */
    public static final String TRACE_CHANNEL_NAME_PROPERTY_NAME = Messages.TraceControl_ChannelNamePropertyName;
    /**
     * The trace channel 'state' property name.
     */
    public static final String TRACE_CHANNEL_STATE_PROPERTY_NAME = Messages.TraceControl_StatePropertyName;
    /**
     *  The trace channel 'overwrite mode' property name.
     */
    public static final String TRACE_CHANNEL_OVERWRITE_MODE_PROPERTY_NAME = Messages.TraceControl_OverwriteModePropertyName;
    /**
     *  The trace channel 'sub-buffer size' property name.
     */
    public static final String TRACE_CHANNEL_SUBBUFFER_SIZE_PROPERTY_NAME = Messages.TraceControl_SubBufferSizePropertyName;
    /**
     *  The trace channel 'sub-buffer size' property name.
     */
    public static final String TRACE_CHANNEL_NO_SUBBUFFERS_PROPERTY_NAME = Messages.TraceControl_NbSubBuffersPropertyName;
    /**
     *  The trace channel 'switch timer interval' property name.
     */
    public static final String TRACE_CHANNEL_SWITCH_TIMER_PROPERTY_NAME = Messages.TraceControl_SwitchTimerPropertyName;
    /**
     *  The trace channel 'read timer interval' property name.
     */
    public static final String TRACE_CHANNEL_READ_TIMER_PROPERTY_NAME = Messages.TraceControl_ReadTimerPropertyName;
    /**
     *  The trace channel 'output type' property name.
     */
    public static final String TRACE_CHANNEL_OUTPUT_TYPEPROPERTY_NAME = Messages.TraceControl_OutputTypePropertyName;
    /**
     * The trace channel 'trace file count' property name.
     */
    public static final String TRACE_CHANNEL_TRACE_FILE_COUNT_PROPERTY_NAME = Messages.TraceControl_TraceFileCountPropertyName;
    /**
     * The trace channel 'trace file size' property name.
     */
    public static final String TRACE_CHANNEL_TRACE_FILE_SIZE_PROPERTY_NAME = Messages.TraceControl_TraceFileSizePropertyName;
    /**
     * The trace channel 'discarded events' property name.
     */
    public static final String TRACE_CHANNEL_DISCARDED_EVENTS_PROPERTY_NAME = Messages.TraceControl_NumberOfDiscardedEventsPropertyName;
    /**
     * The trace channel 'lost packets' property name.
     */
    public static final String TRACE_CHANNEL_LOST_PACKETS_PROPERTY_NAME = Messages.TraceControl_NumberOfLostPacketsPropertyName;

    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The channel component which this property source is for.
     */
    private final TraceChannelComponent fChannel;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param component - the channel component
     */
    public TraceChannelPropertySource(TraceChannelComponent component) {
        fChannel = component;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        List<IPropertyDescriptor> properties = new ArrayList<>();
        properties.add(new ReadOnlyTextPropertyDescriptor(TRACE_CHANNEL_NAME_PROPERTY_ID, TRACE_CHANNEL_NAME_PROPERTY_NAME));
        properties.add(new ReadOnlyTextPropertyDescriptor(TRACE_CHANNEL_STATE_PROPERTY_ID, TRACE_CHANNEL_STATE_PROPERTY_NAME));
        properties.add(new ReadOnlyTextPropertyDescriptor(TRACE_CHANNEL_OVERWRITE_MODE_PROPERTY_ID, TRACE_CHANNEL_OVERWRITE_MODE_PROPERTY_NAME));
        properties.add(new ReadOnlyTextPropertyDescriptor(TRACE_CHANNEL_SUBBUFFER_SIZE_PROPERTY_ID, TRACE_CHANNEL_SUBBUFFER_SIZE_PROPERTY_NAME));
        properties.add(new ReadOnlyTextPropertyDescriptor(TRACE_CHANNEL_NO_SUBBUFFERS_PROPERTY_ID, TRACE_CHANNEL_NO_SUBBUFFERS_PROPERTY_NAME));
        properties.add(new ReadOnlyTextPropertyDescriptor(TRACE_CHANNEL_SWITCH_TIMER_PROPERTY_ID, TRACE_CHANNEL_SWITCH_TIMER_PROPERTY_NAME));
        properties.add(new ReadOnlyTextPropertyDescriptor(TRACE_CHANNEL_READ_TIMER_PROPERTY_ID, TRACE_CHANNEL_READ_TIMER_PROPERTY_NAME));
        properties.add(new ReadOnlyTextPropertyDescriptor(TRACE_CHANNEL_OUTPUT_TYPE_PROPERTY_ID, TRACE_CHANNEL_OUTPUT_TYPEPROPERTY_NAME));

        if (fChannel.getTargetNode().isVersionSupported("2.5.0")) { //$NON-NLS-1$
            properties.add(new ReadOnlyTextPropertyDescriptor(TRACE_CHANNEL_TRACE_FILE_COUNT_PROPERTY_ID, TRACE_CHANNEL_TRACE_FILE_COUNT_PROPERTY_NAME));
            properties.add(new ReadOnlyTextPropertyDescriptor(TRACE_CHANNEL_TRACE_FILE_SIZE_PROPERTY_ID, TRACE_CHANNEL_TRACE_FILE_SIZE_PROPERTY_NAME));
        }
        if (fChannel.getTargetNode().isVersionSupported("2.8.0")) { //$NON-NLS-1$
            properties.add(new ReadOnlyTextPropertyDescriptor(TRACE_CHANNEL_DISCARDED_EVENTS_PROPERTY_ID, TRACE_CHANNEL_DISCARDED_EVENTS_PROPERTY_NAME));
            properties.add(new ReadOnlyTextPropertyDescriptor(TRACE_CHANNEL_LOST_PACKETS_PROPERTY_ID, TRACE_CHANNEL_LOST_PACKETS_PROPERTY_NAME));
        }
        return properties.toArray(new IPropertyDescriptor[0]);
    }

    @Override
    public Object getPropertyValue(Object id) {
        if(TRACE_CHANNEL_NAME_PROPERTY_ID.equals(id)) {
            return fChannel.getName();
        }
        if (TRACE_CHANNEL_STATE_PROPERTY_ID.equals(id)) {
            return fChannel.getState().name();
        }
        if(TRACE_CHANNEL_OVERWRITE_MODE_PROPERTY_ID.equals(id)) {
            return String.valueOf(fChannel.isOverwriteMode());
        }
        if(TRACE_CHANNEL_SUBBUFFER_SIZE_PROPERTY_ID.equals(id)) {
            return String.valueOf(fChannel.getSubBufferSize());
        }
        if(TRACE_CHANNEL_NO_SUBBUFFERS_PROPERTY_ID.equals(id)) {
            return String.valueOf(fChannel.getNumberOfSubBuffers());
        }
        if(TRACE_CHANNEL_SWITCH_TIMER_PROPERTY_ID.equals(id)) {
            return String.valueOf(fChannel.getSwitchTimer());
        }
        if(TRACE_CHANNEL_READ_TIMER_PROPERTY_ID.equals(id)) {
            return String.valueOf(fChannel.getReadTimer());
        }
        if(TRACE_CHANNEL_OUTPUT_TYPE_PROPERTY_ID.equals(id)) {
            return fChannel.getOutputType().getInName();
        }
        if (TRACE_CHANNEL_TRACE_FILE_COUNT_PROPERTY_ID.equals(id)) {
            return fChannel.getMaxNumberTraceFiles();
        }
        if (TRACE_CHANNEL_TRACE_FILE_SIZE_PROPERTY_ID.equals(id)) {
            return fChannel.getMaxSizeTraceFiles();
        }
        if (TRACE_CHANNEL_DISCARDED_EVENTS_PROPERTY_ID.equals(id)) {
            return fChannel.getNumberOfDiscardedEvents();
        }
        if (TRACE_CHANNEL_LOST_PACKETS_PROPERTY_ID.equals(id)) {
            return fChannel.getNumberOfLostPackets();
        }

        return null;
    }

}
