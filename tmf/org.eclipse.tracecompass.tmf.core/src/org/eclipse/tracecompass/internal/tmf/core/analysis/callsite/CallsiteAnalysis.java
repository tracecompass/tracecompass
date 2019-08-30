/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.analysis.callsite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.analysis.callsite.ITmfCallsiteResolver;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCallsiteAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfCallsite;
import org.eclipse.tracecompass.tmf.core.event.lookup.TmfCallsite;
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
        Iterable<ITmfEventAspect<?>> cpus = TmfTraceUtils.getEventAspects(trace, TmfCpuAspect.class);
        Iterable<ITmfEventAspect<?>>  callsites = TmfTraceUtils.getEventAspects(trace, TmfCallsiteAspect.class);
        return trace.getUUID() != null && !Iterables.isEmpty(cpus) && !Iterables.isEmpty(callsites) && super.canExecute(trace);
    }

    @Override
    protected @NonNull ITmfStateProvider createStateProvider() {
        ITmfTrace trace = getTrace();
        return new CallsiteStateProvider(Objects.requireNonNull(trace), CallsiteStateProvider.ID, fSSInterner);
    }

    @Override
    public List<ITmfCallsite> getCallsites(String traceId, String device, long time) {
        ITmfStateSystem ss = getStateSystem();
        if (ss == null) {
            return Collections.emptyList();
        }
        int quark = ss.optQuarkAbsolute(CallsiteStateProvider.DEVICES, traceId, device);
        int invalidAttribute = ITmfStateSystem.INVALID_ATTRIBUTE;
        if (quark == invalidAttribute) {
            return Collections.emptyList();
        }
        int stringPool = ss.optQuarkAbsolute(CallsiteStateProvider.STRING_POOL);
        int fileQuark = ss.optQuarkRelative(quark, CallsiteStateProvider.FILES);
        int lineQuark = ss.optQuarkRelative(quark, CallsiteStateProvider.LINES);
        if (fileQuark == ITmfStateSystem.INVALID_ATTRIBUTE || lineQuark == ITmfStateSystem.INVALID_ATTRIBUTE || stringPool == ITmfStateSystem.INVALID_ATTRIBUTE) {
            return Collections.emptyList();
        }
        List<Integer> files = ss.getSubAttributes(fileQuark, false);
        List<Integer> lines = ss.getSubAttributes(lineQuark, false);
        int size = files.size();
        if (size != lines.size()) {
            return Collections.emptyList();
        }
        List<ITmfCallsite> retVal = new ArrayList<>();
        try {
            List<ITmfStateInterval> values = ss.queryFullState(time);
            Object stringIntern = values.get(fileQuark).getValue();
            int lineNo = values.get(lineQuark).getValueInt();
            if (lineNo != CallsiteStateProvider.UNKNOWN_LINE_NO && stringIntern instanceof Integer) {
                String fileName = fSSInterner.resolve(ss, (Integer) stringIntern + ss.getStartTime(), stringPool);
                if (fileName != null) {
                    retVal.add(new TmfCallsite(fileName, (long) lineNo));
                }
            }

        } catch (StateSystemDisposedException e) {
            // swallow it
        }
        return retVal;
    }
}
