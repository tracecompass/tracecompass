/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.latency;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.KernelTidAspect;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelTrace;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.AbstractSegmentStoreAnalysisModule;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.segment.ISegmentAspect;

import com.google.common.collect.ImmutableList;

/**
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public class SystemCallLatencyAnalysis extends AbstractSegmentStoreAnalysisModule {

    /**
     * The ID of this analysis
     */
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.latency.syscall"; //$NON-NLS-1$

    private static final String DATA_FILENAME = "latency-analysis.dat"; //$NON-NLS-1$

    private static final Collection<ISegmentAspect> BASE_ASPECTS =
            ImmutableList.of(SyscallNameAspect.INSTANCE);

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public Iterable<ISegmentAspect> getSegmentAspects() {
        return BASE_ASPECTS;
    }

    @Override
    public @NonNull String getDataFileName() {
        return DATA_FILENAME;
    }

    @Override
    public AbstractSegmentStoreAnalysisRequest createAnalysisRequest(ISegmentStore<ISegment> syscalls) {
        return new SyscallLatencyAnalysisRequest(syscalls);
    }

    @Override
    protected Object[] readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        return checkNotNull((Object[]) ois.readObject());
    }

    private static class SyscallLatencyAnalysisRequest extends AbstractSegmentStoreAnalysisRequest {

        private final Map<Integer, SystemCall.InitialInfo> fOngoingSystemCalls = new HashMap<>();
        private @Nullable IKernelAnalysisEventLayout fLayout;

        public SyscallLatencyAnalysisRequest(ISegmentStore<ISegment> syscalls) {
            super(syscalls);
        }

        @Override
        public void handleData(final ITmfEvent event) {
            super.handleData(event);
            IKernelAnalysisEventLayout layout = fLayout;
            if (layout == null) {
                IKernelTrace trace = (IKernelTrace) event.getTrace();
                layout = trace.getKernelEventLayout();
                fLayout = layout;
            }
            final String eventName = event.getType().getName();

            if (eventName.startsWith(layout.eventSyscallEntryPrefix()) ||
                    eventName.startsWith(layout.eventCompatSyscallEntryPrefix())) {
                /* This is a system call entry event */

                Integer tid = KernelTidAspect.INSTANCE.resolve(event);
                if (tid == null) {
                    // no information on this event/trace ?
                    return;
                }

                /* Record the event's data into the intial system call info */
                // String syscallName = fLayout.getSyscallNameFromEvent(event);
                long startTime = event.getTimestamp().getValue();
                String syscallName = eventName.substring(layout.eventSyscallEntryPrefix().length());

                Map<String, String> args = event.getContent().getFieldNames().stream()
                    .collect(Collectors.toMap(Function.identity(),
                            input -> checkNotNull(event.getContent().getField(input).getValue().toString())));

                SystemCall.InitialInfo newSysCall = new SystemCall.InitialInfo(startTime, checkNotNull(syscallName), checkNotNull(args));
                fOngoingSystemCalls.put(tid, newSysCall);

            } else if (eventName.startsWith(layout.eventSyscallExitPrefix())) {
                /* This is a system call exit event */

                Integer tid = KernelTidAspect.INSTANCE.resolve(event);
                if (tid == null) {
                    return;
                }

                SystemCall.InitialInfo info = fOngoingSystemCalls.remove(tid);
                if (info == null) {
                    /*
                     * We have not seen the entry event corresponding to this
                     * exit (lost event, or before start of trace).
                     */
                    return;
                }

                long endTime = event.getTimestamp().getValue();
                int ret = ((Long) event.getContent().getField("ret").getValue()).intValue(); //$NON-NLS-1$
                ISegment syscall = new SystemCall(info, endTime, ret);
                getSegmentStore().add(syscall);
            }
        }

        @Override
        public void handleCompleted() {
            fOngoingSystemCalls.clear();
        }
    }

    private static class SyscallNameAspect implements ISegmentAspect {
        public static final ISegmentAspect INSTANCE = new SyscallNameAspect();

        private SyscallNameAspect() { }

        @Override
        public String getHelpText() {
            return checkNotNull(Messages.SegmentAspectHelpText_SystemCall);
        }
        @Override
        public String getName() {
            return checkNotNull(Messages.SegmentAspectName_SystemCall);
        }
        @Override
        public @Nullable Comparator<?> getComparator() {
            return null;
        }
        @Override
        public @Nullable String resolve(ISegment segment) {
            if (segment instanceof SystemCall) {
                return ((SystemCall) segment).getName();
            }
            return EMPTY_STRING;
        }
    }

}
