/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.latency;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelTidAspect;
import org.eclipse.tracecompass.analysis.os.linux.core.model.OsStrings;
import org.eclipse.tracecompass.analysis.os.linux.core.tid.TidAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelTrace;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.AbstractSegmentStoreAnalysisEventBasedModule;
import org.eclipse.tracecompass.datastore.core.interval.IHTIntervalReader;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.SegmentStoreFactory.SegmentStoreType;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.segment.ISegmentAspect;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public class SystemCallLatencyAnalysis extends AbstractSegmentStoreAnalysisEventBasedModule {

    /**
     * The ID of this analysis
     */
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.latency.syscall"; //$NON-NLS-1$
    private static final String RET_FIELD = "ret"; //$NON-NLS-1$
    private static final int VERSION = 2;

    private static final Collection<ISegmentAspect> BASE_ASPECTS =
            ImmutableList.of(SyscallNameAspect.INSTANCE, SyscallTidAspect.INSTANCE, SyscallRetAspect.INSTANCE);

    @Override
    public String getId() {
        return ID;
    }

    @Override
    protected Iterable<IAnalysisModule> getDependentAnalyses() {
        ITmfTrace trace = getTrace();
        if (trace == null) {
            throw new IllegalStateException();
        }
        IAnalysisModule module = trace.getAnalysisModule(TidAnalysisModule.ID);
        if (module == null) {
            return Collections.emptySet();
        }
        return ImmutableSet.of(module);
    }

    @Override
    public Iterable<ISegmentAspect> getSegmentAspects() {
        return BASE_ASPECTS;
    }

    @Override
    protected int getVersion() {
        return VERSION;
    }

    @Override
    protected @NonNull SegmentStoreType getSegmentStoreType() {
        return SegmentStoreType.OnDisk;
    }

    @Override
    protected AbstractSegmentStoreAnalysisRequest createAnalysisRequest(ISegmentStore<ISegment> syscalls, IProgressMonitor monitor) {
        return new SyscallLatencyAnalysisRequest(syscalls, monitor);
    }

    @Override
    protected @NonNull IHTIntervalReader<ISegment> getSegmentReader() {
        return SystemCall.READER;
    }

    private class SyscallLatencyAnalysisRequest extends AbstractSegmentStoreAnalysisRequest {

        private final Map<Integer, SystemCall.InitialInfo> fOngoingSystemCalls = new HashMap<>();
        private @Nullable IKernelAnalysisEventLayout fLayout;
        private final IProgressMonitor fMonitor;

        public SyscallLatencyAnalysisRequest(ISegmentStore<ISegment> syscalls, IProgressMonitor monitor) {
            super(syscalls);
            fMonitor = monitor;
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
            final String eventName = event.getName();

            if (eventName.startsWith(layout.eventSyscallEntryPrefix()) ||
                    eventName.startsWith(layout.eventCompatSyscallEntryPrefix())) {
                /* This is a system call entry event */

                Integer tid;
                try {
                    tid = KernelTidAspect.INSTANCE.resolve(event, true, fMonitor);
                } catch (InterruptedException e) {
                    return;
                }
                if (tid == null) {
                    // no information on this event/trace ?
                    return;
                }

                /* Record the event's data into the intial system call info */
                // String syscallName = fLayout.getSyscallNameFromEvent(event);
                long startTime = event.getTimestamp().toNanos();
                String syscallName = eventName.substring(layout.eventSyscallEntryPrefix().length());

                SystemCall.InitialInfo newSysCall = new SystemCall.InitialInfo(startTime, syscallName.intern(), tid);
                fOngoingSystemCalls.put(tid, newSysCall);

            } else if (eventName.startsWith(layout.eventSyscallExitPrefix())) {
                /* This is a system call exit event */

                Integer tid;
                try {
                    tid = KernelTidAspect.INSTANCE.resolve(event, true, fMonitor);
                } catch (InterruptedException e) {
                    return;
                }
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

                long endTime = event.getTimestamp().toNanos();
                Integer ret = event.getContent().getFieldValue(Integer.class, RET_FIELD);
                SystemCall syscall = new SystemCall(info, endTime, ret == null ? -1 : ret);
                getSegmentStore().add(syscall);
            }
        }

        @Override
        public void handleCompleted() {
            fOngoingSystemCalls.clear();
            super.handleCompleted();
        }

        @Override
        public void handleCancel() {
            fMonitor.setCanceled(true);
            super.handleCancel();
        }
    }

    private static final class SyscallNameAspect implements ISegmentAspect {
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

    private static final class SyscallTidAspect implements ISegmentAspect {
        public static final ISegmentAspect INSTANCE = new SyscallTidAspect();

        private SyscallTidAspect() { }

        @Override
        public String getHelpText() {
            return checkNotNull(Messages.SegmentAspectHelpText_SystemCallTid);
        }
        @Override
        public String getName() {
            return OsStrings.tid();
        }
        @Override
        public @Nullable Comparator<?> getComparator() {
            return null;
        }
        @Override
        public @Nullable Integer resolve(ISegment segment) {
            if (segment instanceof SystemCall) {
                return ((SystemCall) segment).getTid();
            }
            return -1;
        }
    }

    private static final class SyscallRetAspect implements ISegmentAspect {
        public static final ISegmentAspect INSTANCE = new SyscallRetAspect();

        private SyscallRetAspect() { }

        @Override
        public String getHelpText() {
            return checkNotNull(Messages.SegmentAspectHelpText_SystemCallRet);
        }
        @Override
        public String getName() {
            return checkNotNull(Messages.SegmentAspectName_SystemCallRet);
        }
        @Override
        public @Nullable Comparator<?> getComparator() {
            return null;
        }
        @Override
        public @Nullable Integer resolve(ISegment segment) {
            if (segment instanceof SystemCall) {
                return ((SystemCall) segment).getReturnValue();
            }
            return -1;
        }
    }

}
