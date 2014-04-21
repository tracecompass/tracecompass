/**********************************************************************
 * Copyright (c) 2014 Ericsson, École Polytechnique de Montréal
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

package org.eclipse.linuxtools.internal.lttng2.ust.core.memoryusage;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.linuxtools.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.ctf.core.CtfTmfEvent;

/**
 * State provider to track the memory of the threads using the UST libc wrapper
 * memory events.
 *
 * @author Matthew Khouzam
 * @author Geneviève Bastien
 */
public class MemoryUsageStateProvider extends AbstractTmfStateProvider {

    /* Version of this state provider */
    private static final int VERSION = 1;

    /* Maps a pointer to a memory zone to the size of the memory */
    private final Map<Long, Long> fMemory = new HashMap<>();

    private static final Long MINUS_ONE = Long.valueOf(-1);
    private static final Long ZERO = Long.valueOf(0);
    private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    /**
     * Constructor
     *
     * @param trace
     *            trace
     */
    public MemoryUsageStateProvider(LttngUstTrace trace) {
        super(trace, CtfTmfEvent.class, "Ust:Memory"); //$NON-NLS-1$
    }

    @Override
    protected void eventHandle(ITmfEvent event) {
        String name = event.getType().getName();
        switch (name) {
        case UstMemoryStrings.MALLOC: {
            Long ptr = (Long) event.getContent().getField(UstMemoryStrings.FIELD_PTR).getValue();
            if (ZERO.equals(ptr)) {
                return;
            }
            Long size = (Long) event.getContent().getField(UstMemoryStrings.FIELD_SIZE).getValue();
            setMem(event, ptr, size);
        }
            break;
        case UstMemoryStrings.FREE: {
            Long ptr = (Long) event.getContent().getField(UstMemoryStrings.FIELD_PTR).getValue();
            if (ZERO.equals(ptr)) {
                return;
            }
            setMem(event, ptr, ZERO);
        }
            break;
        case UstMemoryStrings.CALLOC: {
            Long ptr = (Long) event.getContent().getField(UstMemoryStrings.FIELD_PTR).getValue();
            if (ZERO.equals(ptr)) {
                return;
            }
            Long nmemb = (Long) event.getContent().getField(UstMemoryStrings.FIELD_NMEMB).getValue();
            Long size = (Long) event.getContent().getField(UstMemoryStrings.FIELD_SIZE).getValue();
            setMem(event, ptr, size * nmemb);
        }
            break;
        case UstMemoryStrings.REALLOC: {
            Long ptr = (Long) event.getContent().getField(UstMemoryStrings.FIELD_PTR).getValue();
            if (ZERO.equals(ptr)) {
                return;
            }
            Long newPtr = (Long) event.getContent().getField(UstMemoryStrings.FIELD_INPTR).getValue();
            Long size = (Long) event.getContent().getField(UstMemoryStrings.FIELD_SIZE).getValue();
            setMem(event, ptr, ZERO);
            setMem(event, newPtr, size);
        }
            break;
        case UstMemoryStrings.MEMALIGN: {
            Long ptr = (Long) event.getContent().getField(UstMemoryStrings.FIELD_PTR).getValue();
            if (ZERO.equals(ptr)) {
                return;
            }
            Long size = (Long) event.getContent().getField(UstMemoryStrings.FIELD_SIZE).getValue();
            setMem(event, ptr, size);
        }
            break;
        case UstMemoryStrings.POSIX_MEMALIGN: {
            Long ptr = (Long) event.getContent().getField(UstMemoryStrings.FIELD_OUTPTR).getValue();
            if (ZERO.equals(ptr)) {
                return;
            }
            Long size = (Long) event.getContent().getField(UstMemoryStrings.FIELD_SIZE).getValue();
            setMem(event, ptr, size);
        }
            break;
        default:
            break;
        }

    }

    @Override
    public ITmfStateProvider getNewInstance() {
        return new MemoryUsageStateProvider(getTrace());
    }

    @Override
    public LttngUstTrace getTrace() {
        return (LttngUstTrace) super.getTrace();
    }

    @Override
    public int getVersion() {
        return VERSION;
    }

    private static Long getVtid(ITmfEvent event) {
        ITmfEventField field = event.getContent().getField(UstMemoryStrings.CONTEXT_VTID);
        if (field == null) {
            return MINUS_ONE;
        }
        return (Long) field.getValue();
    }

    private static String getProcname(ITmfEvent event) {
        ITmfEventField field = event.getContent().getField(UstMemoryStrings.CONTEXT_PROCNAME);
        if (field == null) {
            return EMPTY_STRING;
        }
        return (String) field.getValue();
    }

    private void setMem(ITmfEvent event, Long ptr, Long size) {
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
