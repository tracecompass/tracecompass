/*******************************************************************************
 * Copyright (c) 2013, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jean-Christian Kouamé - Initial API and implementation
 *     Patrick Tasse - Updates to mipmap feature
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.statesystem.mipmap;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.internal.tmf.core.statesystem.mipmap.AbstractTmfMipmapStateProvider;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventType;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventType;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub;

/**
 * A mipmap state provider for test
 *
 * @author Jean-Christian Kouamé
 */
class TmfMipmapStateProviderStub extends AbstractTmfMipmapStateProvider {
    /** test attribute name */
    public final static String TEST_ATTRIBUTE_NAME = "test_attribute"; //$NON-NLS-1$

    private int resolution;
    private ITmfStateValue.Type type;
    private static final @NonNull String MIPMAP_ID = "MIPMAP_ID"; //$NON-NLS-1$

    private final String ERROR_INVALID_STATE_VALUE = "Error : Invalid state value"; //$NON-NLS-1$
    private final String ERROR_INVALID_TIMESTAMP = "Error : Invalid timestamp"; //$NON-NLS-1$

    /**
     * Constructor
     *
     * @param resolution
     *            the mipmap resolution array (max, min, avg)
     * @param type
     *            the type of value to use
     */
    public TmfMipmapStateProviderStub(int resolution, ITmfStateValue.Type type) {
        super(new TmfTraceStub(), MIPMAP_ID);
        this.resolution = resolution;
        this.type = type;
    }

    @Override
    public void dispose() {
        super.dispose();
        getTrace().dispose();
    }

    @Override
    protected void eventHandle(ITmfEvent ev) {
        ITmfStateSystemBuilder ss = checkNotNull(getStateSystemBuilder());
        final long ts = ev.getTimestamp().toNanos();
        try {
            int quark = ss.getQuarkAbsoluteAndAdd(TEST_ATTRIBUTE_NAME);
            ITmfStateValue value = (ITmfStateValue) ev.getContent().getValue();
            modifyMipmapAttribute(ts, value, quark, MIN | MAX | AVG, resolution);
        } catch (TimeRangeException e) {
            Activator.logError(ERROR_INVALID_TIMESTAMP, e);
        } catch (StateValueTypeException e) {
            Activator.logError(ERROR_INVALID_STATE_VALUE, e);
        }
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public TmfMipmapStateProviderStub getNewInstance() {
        return new TmfMipmapStateProviderStub(resolution, type);
    }

    /**
     * @param time
     *            The event type
     * @param longVal
     *            The event value or null
     * @return A new TmfEvent
     */
    public @NonNull ITmfEvent createEvent(long time, Long longVal) {
        ITmfStateValue value;
        if (longVal == null) {
            value = TmfStateValue.nullValue();
        } else if (type == ITmfStateValue.Type.LONG) {
            value = TmfStateValue.newValueLong(longVal);
        } else if (type == ITmfStateValue.Type.INTEGER) {
            value = TmfStateValue.newValueInt(longVal.intValue());
        } else if (type == ITmfStateValue.Type.DOUBLE) {
            value = TmfStateValue.newValueDouble(longVal.doubleValue());
        } else {
            value = TmfStateValue.nullValue();
        }
        ITmfTimestamp timestamp = TmfTimestamp.fromNanos(time);
        ITmfEventType eventType = new TmfEventType(MIPMAP_ID, null);
        ITmfEventField content = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, value, null);
        ITmfEvent event = new TmfEvent(null, ITmfContext.UNKNOWN_RANK, timestamp, eventType, content);
        return event;
    }
}
