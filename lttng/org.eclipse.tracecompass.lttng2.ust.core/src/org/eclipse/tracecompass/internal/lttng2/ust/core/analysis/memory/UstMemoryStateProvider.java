/**********************************************************************
 * Copyright (c) 2014, 2015 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Geneviève Bastien - Memory is per thread and only total is kept
 **********************************************************************/

package org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.memory;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.tracecompass.lttng2.ust.core.trace.layout.ILttngUstEventLayout;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;

import com.google.common.collect.ImmutableMap;

/**
 * State provider to track the memory of the threads using the UST libc wrapper
 * memory events.
 *
 * @author Matthew Khouzam
 * @author Geneviève Bastien
 */
public class UstMemoryStateProvider extends AbstractTmfStateProvider {

    /* Version of this state provider */
    private static final int VERSION = 1;

    private static final Long MINUS_ONE = Long.valueOf(-1);
    private static final Long ZERO = Long.valueOf(0);
    private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    private static final int MALLOC_INDEX = 1;
    private static final int FREE_INDEX = 2;
    private static final int CALLOC_INDEX = 3;
    private static final int REALLOC_INDEX = 4;
    private static final int MEMALIGN_INDEX = 5;
    private static final int POSIX_MEMALIGN_INDEX = 6;

    /** Map of a pointer to a memory zone to the size of the memory */
    private final Map<Long, Long> fMemory = new HashMap<>();

    private final @NonNull ILttngUstEventLayout fLayout;
    private final @NonNull Map<String, Integer> fEventNames;

    /**
     * Constructor
     *
     * @param trace
     *            trace
     */
    public UstMemoryStateProvider(@NonNull LttngUstTrace trace) {
        super(trace, "Ust:Memory"); //$NON-NLS-1$
        fLayout = trace.getEventLayout();
        fEventNames = buildEventNames(fLayout);
    }

    private static @NonNull Map<String, Integer> buildEventNames(ILttngUstEventLayout layout) {
        ImmutableMap.Builder<String, Integer> builder = ImmutableMap.builder();
        builder.put(layout.eventLibcMalloc(), MALLOC_INDEX);
        builder.put(layout.eventLibcFree(), FREE_INDEX);
        builder.put(layout.eventLibcCalloc(), CALLOC_INDEX);
        builder.put(layout.eventLibcRealloc(), REALLOC_INDEX);
        builder.put(layout.eventLibcMemalign(), MEMALIGN_INDEX);
        builder.put(layout.eventLibcPosixMemalign(), POSIX_MEMALIGN_INDEX);
        return builder.build();
    }

    @Override
    protected void eventHandle(ITmfEvent event) {
        String name = event.getName();
        Integer index = fEventNames.get(name);
        int intIndex = (index == null ? -1 : index.intValue());

        switch (intIndex) {
        case MALLOC_INDEX: {
            Long ptr = (Long) event.getContent().getField(fLayout.fieldPtr()).getValue();
            if (ZERO.equals(ptr)) {
                return;
            }
            Long size = (Long) event.getContent().getField(fLayout.fieldSize()).getValue();
            setMem(event, ptr, size);
        }
            break;
        case FREE_INDEX: {
            Long ptr = (Long) event.getContent().getField(fLayout.fieldPtr()).getValue();
            if (ZERO.equals(ptr)) {
                return;
            }
            setMem(event, ptr, ZERO);
        }
            break;
        case CALLOC_INDEX: {
            Long ptr = (Long) event.getContent().getField(fLayout.fieldPtr()).getValue();
            if (ZERO.equals(ptr)) {
                return;
            }
            Long nmemb = (Long) event.getContent().getField(fLayout.fieldNmemb()).getValue();
            Long size = (Long) event.getContent().getField(fLayout.fieldSize()).getValue();
            setMem(event, ptr, size * nmemb);
        }
            break;
        case REALLOC_INDEX: {
            Long ptr = (Long) event.getContent().getField(fLayout.fieldPtr()).getValue();
            if (ZERO.equals(ptr)) {
                return;
            }
            Long newPtr = (Long) event.getContent().getField(fLayout.fieldInPtr()).getValue();
            Long size = (Long) event.getContent().getField(fLayout.fieldSize()).getValue();
            setMem(event, ptr, ZERO);
            setMem(event, newPtr, size);
        }
            break;
        case MEMALIGN_INDEX: {
            Long ptr = (Long) event.getContent().getField(fLayout.fieldPtr()).getValue();
            if (ZERO.equals(ptr)) {
                return;
            }
            Long size = (Long) event.getContent().getField(fLayout.fieldSize()).getValue();
            setMem(event, ptr, size);
        }
            break;
        case POSIX_MEMALIGN_INDEX: {
            Long ptr = (Long) event.getContent().getField(fLayout.fieldOutPtr()).getValue();
            if (ZERO.equals(ptr)) {
                return;
            }
            Long size = (Long) event.getContent().getField(fLayout.fieldSize()).getValue();
            setMem(event, ptr, size);
        }
            break;
        default:
            /* Ignore other event types */
            break;
        }

    }

    @Override
    public ITmfStateProvider getNewInstance() {
        return new UstMemoryStateProvider(getTrace());
    }

    @Override
    public LttngUstTrace getTrace() {
        return (LttngUstTrace) super.getTrace();
    }

    @Override
    public int getVersion() {
        return VERSION;
    }

    private Long getVtid(ITmfEvent event) {
        ITmfEventField field = event.getContent().getField(fLayout.contextVtid());
        if (field == null) {
            return MINUS_ONE;
        }
        return (Long) field.getValue();
    }

    private String getProcname(ITmfEvent event) {
        ITmfEventField field = event.getContent().getField(fLayout.contextProcname());
        if (field == null) {
            return EMPTY_STRING;
        }
        return (String) field.getValue();
    }

    private void setMem(ITmfEvent event, Long ptr, Long size) {
        ITmfStateSystemBuilder ss = checkNotNull(getStateSystemBuilder());
        long ts = event.getTimestamp().getValue();
        Long tid = getVtid(event);

        Long memoryDiff = size;
        /* Size is 0, it means it was deleted */
        if (ZERO.equals(size)) {
            Long memSize = fMemory.remove(ptr);
            if (memSize == null) {
                return;
            }
            memoryDiff = -memSize;
        } else {
            fMemory.put(ptr, size);
        }
        try {
            int tidQuark = ss.getQuarkAbsoluteAndAdd(tid.toString());
            int tidMemQuark = ss.getQuarkRelativeAndAdd(tidQuark, UstMemoryStrings.UST_MEMORY_MEMORY_ATTRIBUTE);

            ITmfStateValue prevMem = ss.queryOngoingState(tidMemQuark);
            /* First time we set this value */
            if (prevMem.isNull()) {
                int procNameQuark = ss.getQuarkRelativeAndAdd(tidQuark, UstMemoryStrings.UST_MEMORY_PROCNAME_ATTRIBUTE);
                String procName = getProcname(event);
                /*
                 * No tid/procname for the event for the event, added to a
                 * 'others' thread
                 */
                if (tid.equals(MINUS_ONE)) {
                    procName = UstMemoryStrings.OTHERS;
                }
                ss.modifyAttribute(ts, TmfStateValue.newValueString(procName), procNameQuark);
                prevMem = TmfStateValue.newValueLong(0);
            }

            long prevMemValue = prevMem.unboxLong();
            prevMemValue += memoryDiff.longValue();
            ss.modifyAttribute(ts, TmfStateValue.newValueLong(prevMemValue), tidMemQuark);
        } catch (AttributeNotFoundException | TimeRangeException | StateValueTypeException e) {
            throw new IllegalStateException(e);
        }
    }

}
