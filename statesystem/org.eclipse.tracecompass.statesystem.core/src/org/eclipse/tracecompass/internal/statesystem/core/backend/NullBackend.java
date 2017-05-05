/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.statesystem.core.backend;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.IntegerRangeCondition;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.TimeRangeCondition;
import org.eclipse.tracecompass.statesystem.core.backend.IStateHistoryBackend;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;

/**
 * An implement of a state history back-end to simply discards *all* the
 * intervals it receives. Obviously, no queries can be done on it. It is useful
 * for using with a StateSystem on which you will only want to do "ongoing"
 * requests.
 *
 * @author Alexandre Montplaisir
 */
public class NullBackend implements IStateHistoryBackend {

    private final @NonNull String ssid;

    /**
     * Constructor
     *
     * @param ssid
     *            The state system's id
     */
    public NullBackend(@NonNull String ssid) {
        this.ssid = ssid;
    }

    @Override
    public String getSSID() {
        return ssid;
    }

    @Override
    public long getStartTime() {
        return 0;
    }

    @Override
    public long getEndTime() {
        return 0;
    }

    /**
     * The interval will be discarded when using a null backend.
     * @deprecated use insertPastState(long stateStartTime, long stateEndTime,
            int quark, Object value) instead
     */
    @Deprecated
    @Override
    public void insertPastState(long stateStartTime, long stateEndTime,
            int quark, ITmfStateValue value) {
        /* The interval is always discarded. */
    }

    /**
     * The interval will be discarded when using a null backend.
     */
    @Override
    public void insertPastState(long stateStartTime, long stateEndTime,
            int quark, Object value) {
        /* The interval is always discarded. */
    }

    @Override
    public void finishedBuilding(long endTime) {
        /* Nothing to do */
    }

    @Override
    public FileInputStream supplyAttributeTreeReader() {
        return null;
    }

    @Override
    public File supplyAttributeTreeWriterFile() {
        return null;
    }

    @Override
    public long supplyAttributeTreeWriterFilePosition() {
        return -1;
    }

    @Override
    public void removeFiles() {
        /* Nothing to do */
    }

    @Override
    public void dispose() {
        /* Nothing to do */
    }

    /**
     * Null back-ends cannot run queries. Nothing will be put in
     * currentStateInfo.
     */
    @Override
    public void doQuery(List<ITmfStateInterval> currentStateInfo, long t) {
        /* Cannot do past queries */
    }

    /**
     * Null back-ends cannot run queries. 'null' will be returned.
     *
     * @return Always returns null.
     */
    @Override
    public ITmfStateInterval doSingularQuery(long t, int attributeQuark) {
        /* Cannot do past queries */
        return null;
    }

    @Override
    public Iterable<@NonNull ITmfStateInterval> query2D(IntegerRangeCondition quarks,
            TimeRangeCondition times) throws TimeRangeException {
        return Collections.EMPTY_LIST;
    }
}
