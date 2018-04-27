/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.handlers;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.regex.Pattern;

import org.eclipse.tracecompass.analysis.os.linux.core.event.aspect.LinuxTidAspect;
import org.eclipse.tracecompass.analysis.os.linux.core.inputoutput.Attributes;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.InputOutputStateProvider;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.handlers.KernelEventHandler;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.StateSystemBuilderUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * System call exit handler
 *
 * @author Houssem Daoud
 */
public class SysExitHandler extends KernelEventHandler {

    private static final String SYSCALL_READ_PATTERN = "[p]?read.*"; //$NON-NLS-1$
    private static final String SYSCALL_WRITE_PATTERN = "[p]?write.*"; //$NON-NLS-1$

    private final Pattern fSyscallReadPattern;
    private final Pattern fSyscallWritePattern;

    /**
     * Constructor
     *
     * @param layout
     *            event layout
     */
    public SysExitHandler(IKernelAnalysisEventLayout layout) {
        super(layout);
        fSyscallReadPattern = checkNotNull(Pattern.compile('(' + layout.eventSyscallEntryPrefix() + '|' + layout.eventCompatSyscallEntryPrefix() + ')' + SYSCALL_READ_PATTERN));
        fSyscallWritePattern = checkNotNull(Pattern.compile('(' + layout.eventSyscallEntryPrefix() + '|' + layout.eventCompatSyscallEntryPrefix() + ')' + SYSCALL_WRITE_PATTERN));
    }

    @Override
    public void handleEvent(ITmfStateSystemBuilder ss, ITmfEvent event) throws AttributeNotFoundException {
        ITmfEventField content = event.getContent();
        long ts = event.getTimestamp().getValue();

        Integer tid = TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), LinuxTidAspect.class, event);
        if (tid == null) {
            return;
        }

        /* TODO: Why save the syscall before if we have it in the sys_exit? */
        int syscallQuark = ss.optQuarkRelative(InputOutputStateProvider.getNodeSyscalls(ss), String.valueOf(tid));
        if (syscallQuark == ITmfStateSystem.INVALID_ATTRIBUTE) {
            return;
        }
        String syscallValue = ss.queryOngoingState(syscallQuark).unboxStr();
        Long retValue = content.getFieldValue(Long.class, getLayout().fieldSyscallRet());
        if (retValue != null) {
            int ret = retValue.intValue();
            if (ret >= 0) {
                if (fSyscallReadPattern.matcher(syscallValue).matches()) {
                    int currentProcessNode = ss.getQuarkRelativeAndAdd(InputOutputStateProvider.getNodeThreads(ss), String.valueOf(tid));
                    int readQuark = ss.getQuarkRelativeAndAdd(currentProcessNode, Attributes.BYTES_READ);
                    ss.getQuarkRelativeAndAdd(currentProcessNode, Attributes.BYTES_WRITTEN);
                    StateSystemBuilderUtils.incrementAttributeInt(ss, ts, readQuark, ret);
                } else if (fSyscallWritePattern.matcher(syscallValue).matches()) {
                    int currentProcessNode = ss.getQuarkRelativeAndAdd(InputOutputStateProvider.getNodeThreads(ss), String.valueOf(tid));
                    ss.getQuarkRelativeAndAdd(currentProcessNode, Attributes.BYTES_READ);
                    int writtenQuark = ss.getQuarkRelativeAndAdd(currentProcessNode, Attributes.BYTES_WRITTEN);
                    StateSystemBuilderUtils.incrementAttributeInt(ss, ts, writtenQuark, ret);
                }
            }
        }

        ss.modifyAttribute(ts, (Object) null, syscallQuark);
    }

}
