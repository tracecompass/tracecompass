/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.analysis.callsite;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCallsiteAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfCallsite;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;

/**
 * Callsite state provider, will write callsites with file names intened to a
 * tree as illustrated below.
 *
 * <pre>
 * +-Devices
 * | |- PATH
 * | |  |- File (top of callsite stack)
 * | |  \- Line (top of callsite stack)
 * | \- n
 * \-Sources(String) (time is the offset. This is used to intern strings)
 * </pre>
 *
 * @author Bernd Hufmann
 * @author Matthew Khouzam (extracted to open source)
 * @since 5.1
 */
public class CallsiteStateProvider extends AbstractTmfStateProvider {

    /**
     * ID
     */
    public static final String ID = "org.eclipse.tracecompass.tmf.core.analysis.callsite"; //$NON-NLS-1$

    /**
     * Devices attribute name
     */
    public static final String DEVICES = "Devices"; //$NON-NLS-1$

    /**
     * Files attribute name
     */
    public static final String FILES = "Files"; //$NON-NLS-1$

    /**
     * Lines attribute name
     */
    public static final String LINES = "Lines"; //$NON-NLS-1$

    /**
     * Unknown line
     */
    public static final int UNKNOWN_LINE_NO = -1;

    /**
     * Unknown category
     */
    public static final Iterable<String> DEFAULT_CATEGORY = Arrays.asList("UNKNOWN"); //$NON-NLS-1$

    /**
     * Strings pool attribute name
     */
    public static final String STRING_POOL = "Sources"; //$NON-NLS-1$

    private int fSourceQuark = ITmfStateSystem.INVALID_ATTRIBUTE;
    private int fDevicesQuark = ITmfStateSystem.INVALID_ATTRIBUTE;

    private final StateSystemStringInterner fInterner;

    /**
     * Instantiate a new state provider.
     *
     * @param trace
     *            The trace
     * @param id
     *            Name given to this state change input. Only used internally.
     * @param interner
     *            the state system string interner
     */
    public CallsiteStateProvider(ITmfTrace trace, String id, StateSystemStringInterner interner) {
        super(trace, id);
        fInterner = interner;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    protected void eventHandle(@NonNull ITmfEvent event) {
        ITmfTrace trace = event.getTrace();
        Iterable<ITmfEventAspect<?>> csAspects = TmfTraceUtils.getEventAspects(trace, TmfCallsiteAspect.class);
        if (!Iterables.isEmpty(csAspects)) {
            ITmfStateSystemBuilder ssb = getStateSystemBuilder();
            if (ssb == null) {
                return;
            }
            int sourceQuark = fSourceQuark;
            if (sourceQuark == ITmfStateSystem.INVALID_ATTRIBUTE) {
                sourceQuark = ssb.getQuarkAbsoluteAndAdd(STRING_POOL);
                fSourceQuark = sourceQuark;
            }
            int devicesQuark = fDevicesQuark;
            if (devicesQuark == ITmfStateSystem.INVALID_ATTRIBUTE) {
                devicesQuark = ssb.getQuarkAbsoluteAndAdd(DEVICES);
                fDevicesQuark = devicesQuark;
            }
            Iterable<ITmfEventAspect<?>> aspects = TmfTraceUtils.getEventAspects(trace, TmfCpuAspect.class);
            String cpu = null;
            for (ITmfEventAspect<?> aspect : aspects) {
                Object result = aspect.resolve(event);
                if (result != null) {
                    cpu = result.toString();
                    break;
                }
            }
            if (cpu == null) {
                return;
            }
            List<String> path = Arrays.asList(String.valueOf(trace.getUUID()), cpu);
            List<ITmfCallsite> callsites = null;
            for (ITmfEventAspect<?> aspect : csAspects) {
                Object result = aspect.resolve(event);
                if (result instanceof List) {
                    callsites = (List<ITmfCallsite>) result;
                    break;
                }
            }
            if (callsites != null && !callsites.isEmpty()) {
                int root = ssb.getQuarkRelativeAndAdd(devicesQuark, Iterables.toArray(path, String.class));
                int filesQuark = ssb.getQuarkRelativeAndAdd(root, FILES);
                int linesQuark = ssb.getQuarkRelativeAndAdd(root, LINES);
                long time = event.getTimestamp().toNanos();
                try {
                    Integer prevFile = (Integer) ssb.queryOngoing(filesQuark);
                    ssb.modifyAttribute(time, (int) (fInterner.intern(ssb, callsites.get(0).getFileName(), sourceQuark) - ssb.getStartTime()), filesQuark);
                    Integer nextFile = (Integer) ssb.queryOngoing(filesQuark);
                    Long lineNo = callsites.get(0).getLineNo();
                    Integer prevLine = (Integer) ssb.queryOngoing(filesQuark);
                    Integer nextLine = lineNo == null ? null : Integer.valueOf(lineNo.intValue());
                    // have at lease one change if a trace has two identifcal callsites.
                    if (Objects.equal(prevLine, nextLine) && Objects.equal(prevFile, nextFile)) {
                        ssb.modifyAttribute(time, (Object) null, linesQuark);
                    }
                    ssb.modifyAttribute(time, nextLine, linesQuark);
                } catch (StateValueTypeException | IndexOutOfBoundsException | TimeRangeException e) {
                    Activator.logError(e.getMessage(), e);
                }
            }
        }

    }

    @Override
    public @NonNull ITmfStateProvider getNewInstance() {
        return new CallsiteStateProvider(getTrace(), ID, new StateSystemStringInterner());
    }

}
