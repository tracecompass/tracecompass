/*******************************************************************************
 * Copyright (c) 2019, 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.analysis.callsite;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.analysis.callsite.ITmfCallsiteIterator;
import org.eclipse.tracecompass.tmf.core.analysis.callsite.ITmfCallsiteResolver;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCallsiteAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfDeviceAspect;
import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfCallsite;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.collect.Iterables;

/**
 * Analysis module for analyzing traces with callsites. It creates a state
 * system with information on source locations of executed events. It can be
 * used to follow program flows.
 *
 * @author Bernd Hufmann
 * @author Matthew Khouzam
 * @since 5.1
 */
public class CallsiteAnalysis extends TmfStateSystemAnalysisModule implements ITmfCallsiteResolver {

    /**
     * ID
     */
    public static final String ID = "org.eclipse.tracecompass.tmf.core.analysis.callsite"; //$NON-NLS-1$

    private final StateSystemStringInterner fSSInterner = new StateSystemStringInterner();

    @Override
    public boolean canExecute(ITmfTrace trace) {
        Iterable<ITmfEventAspect<?>> devices = TmfTraceUtils.getEventAspects(trace, TmfDeviceAspect.class);
        Iterable<ITmfEventAspect<?>> callsites = TmfTraceUtils.getEventAspects(trace, TmfCallsiteAspect.class);
        return trace.getUUID() != null && !Iterables.isEmpty(devices) && !Iterables.isEmpty(callsites) && super.canExecute(trace);
    }

    @Override
    protected @NonNull ITmfStateProvider createStateProvider() {
        ITmfTrace trace = getTrace();
        return new CallsiteStateProvider(Objects.requireNonNull(trace), CallsiteStateProvider.ID, fSSInterner);
    }

    @Override
    public List<ITmfCallsite> getCallsites(String traceId, String deviceType, String deviceId, long time) {
        ITmfCallsiteIterator iterator = iterator(traceId, deviceType, deviceId, time);
        if (iterator.hasPrevious()) {
            return Collections.singletonList(iterator.previous().getCallsite());
        }
        return Collections.emptyList();
    }

    @Override
    public ITmfCallsiteIterator iterator(String traceId, String deviceType, String deviceId, long initialTime) {
        return new CallsiteIterator(getStateSystem(), traceId, deviceType, deviceId, initialTime, fSSInterner);
    }

    /**
     * Returns the state system string interner
     *
     * @return the state system string interner
     */
    protected StateSystemStringInterner getStringInterner() {
        return fSSInterner;
    }
}
