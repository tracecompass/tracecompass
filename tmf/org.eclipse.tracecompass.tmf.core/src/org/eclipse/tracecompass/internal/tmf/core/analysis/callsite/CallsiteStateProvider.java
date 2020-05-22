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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCallsiteAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfDeviceAspect;
import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfCallsite;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * Callsite state provider, will write callsites with file names interned to a
 * tree as illustrated below.
 *
 * <pre>
 * +-Devices
 * | +-<trace UUID>
 * | | +-<device type>
 * | | | +-<device ID>
 * | | | | +- Files(interned string Integer) (top of callsite stack)
 * | | | | \- Lines(Integer)                 (top of callsite stack)
 * | : : :
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

    private int fSourceQuark;
    private int fDevicesQuark;

    private final StateSystemStringInterner fInterner;

    private Iterable<ITmfEventAspect<?>> fCsAspects;

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
        fCsAspects = TmfTraceUtils.getEventAspects(trace, TmfCallsiteAspect.class);
    }

    @Override
    public int getVersion() {
        return 2;
    }

    @Override
    public void assignTargetStateSystem(ITmfStateSystemBuilder ssb) {
        super.assignTargetStateSystem(ssb);
        fSourceQuark = ssb.getQuarkAbsoluteAndAdd(STRING_POOL);
        fDevicesQuark = ssb.getQuarkAbsoluteAndAdd(DEVICES);
    }

    @Override
    protected void eventHandle(@NonNull ITmfEvent event) {
        ITmfStateSystemBuilder ssb = Objects.requireNonNull(getStateSystemBuilder());
        List<ITmfCallsite> callsites = null;
        for (ITmfEventAspect<?> aspect : fCsAspects) {
            Object result = aspect.resolve(event);
            if (result instanceof List) {
                callsites = (List<ITmfCallsite>) result;
                break;
            }
        }
        if (callsites == null || callsites.isEmpty()) {
            return;
        }
        int root = getRootAttribute(event);
        if (root == ITmfStateSystem.INVALID_ATTRIBUTE) {
            return;
        }
        int filesQuark = ssb.getQuarkRelativeAndAdd(root, FILES);
        int linesQuark = ssb.getQuarkRelativeAndAdd(root, LINES);
        long time = event.getTimestamp().toNanos();
        try {
            Integer prevFile = (Integer) ssb.queryOngoing(filesQuark);
            ssb.modifyAttribute(time, (int) (fInterner.intern(ssb, callsites.get(0).getFileName(), fSourceQuark) - ssb.getStartTime()), filesQuark);
            Integer nextFile = (Integer) ssb.queryOngoing(filesQuark);
            Long lineNo = callsites.get(0).getLineNo();
            Integer prevLine = (Integer) ssb.queryOngoing(filesQuark);
            Integer nextLine = lineNo == null ? null : Integer.valueOf(lineNo.intValue());
            // have at least one change if a trace has two identical callsites.
            if (Objects.equals(prevLine, nextLine) && Objects.equals(prevFile, nextFile)) {
                ssb.modifyAttribute(time, (Object) null, linesQuark);
            }
            ssb.modifyAttribute(time, nextLine, linesQuark);
        } catch (StateValueTypeException | IndexOutOfBoundsException | TimeRangeException e) {
            Activator.logError(e.getMessage(), e);
        }
    }

    /**
     * Get or create the root attribute for the specified event. This attribute
     * is the parent of the {@value #FILES} and {@value #LINES} attributes.
     *
     * @param event
     *            the event
     * @return the root attribute or {@link ITmfStateSystem#INVALID_ATTRIBUTE}
     */
    protected int getRootAttribute(ITmfEvent event) {
        String deviceId = null;
        String deviceType = null;
        ITmfTrace trace = event.getTrace();
        for (ITmfEventAspect<?> aspect : trace.getEventAspects()) {
            if (aspect instanceof TmfDeviceAspect) {
                TmfDeviceAspect deviceAspect = (TmfDeviceAspect) aspect;
                Object result = deviceAspect.resolve(event);
                if (result != null) {
                    deviceId = result.toString();
                    deviceType = deviceAspect.getDeviceType();
                    break;
                }
            }
        }
        if (deviceId == null) {
            return ITmfStateSystem.INVALID_ATTRIBUTE;
        }
        ITmfStateSystemBuilder ssb = Objects.requireNonNull(getStateSystemBuilder());
        return ssb.getQuarkRelativeAndAdd(fDevicesQuark, String.valueOf(trace.getUUID()), deviceType, deviceId);
    }

    @Override
    public @NonNull ITmfStateProvider getNewInstance() {
        return new CallsiteStateProvider(getTrace(), ID, new StateSystemStringInterner());
    }

}
