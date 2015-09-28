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
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.KernelTidAspect;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelTrace;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.treemap.TreeMapStore;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

/**
 * @author Alexandre Montplaisir
 * @since 1.1
 */
public class LatencyAnalysis extends TmfAbstractAnalysisModule {

    /**
     * The ID of this analysis
     */
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.latency"; //$NON-NLS-1$

    private static final String DATA_FILENAME = "latency-analysis.dat"; //$NON-NLS-1$

    private @Nullable ISegmentStore<ISegment> fSystemCalls;

    private @Nullable ITmfEventRequest fOngoingRequest = null;

    private final Set<LatencyAnalysisListener> fListeners = new HashSet<>();

    @Override
    public String getId() {
        return ID;
    }

    /**
     * Listener for the viewers
     *
     * @param listener
     *            listener for each type of viewer
     */
    public void addListener(LatencyAnalysisListener listener) {
        fListeners.add(listener);
    }

    @Override
    protected boolean executeAnalysis(IProgressMonitor monitor) throws TmfAnalysisException {
        IKernelTrace trace = checkNotNull((IKernelTrace) getTrace());
        IKernelAnalysisEventLayout layout = trace.getKernelEventLayout();

        /* See if the data file already exists on disk */
        String dir = TmfTraceManager.getSupplementaryFileDir(trace);
        final Path file = Paths.get(dir, DATA_FILENAME);

        if (Files.exists(file)) {
            /* Attempt to read the existing file */
            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file))) {
                @SuppressWarnings("unchecked")
                ISegmentStore<ISegment> syscalls = (ISegmentStore<ISegment>) ois.readObject();
                fSystemCalls = syscalls;
                return true;
            } catch (IOException | ClassNotFoundException | ClassCastException e) {
                /*
                 * We did not manage to read the file successfully, we will just
                 * fall-through to rebuild a new one.
                 */
                try {
                    Files.delete(file);
                } catch (IOException e1) {
                }
            }
        }

        ISegmentStore<ISegment> syscalls = new TreeMapStore<>();

        /* Cancel an ongoing request */
        ITmfEventRequest req = fOngoingRequest;
        if ((req != null) && (!req.isCompleted())) {
            req.cancel();
        }

        /* Create a new request */
        req = new LatencyAnalysisRequest(layout, syscalls);
        fOngoingRequest = req;
        trace.sendRequest(req);

        try {
            req.waitForCompletion();
        } catch (InterruptedException e) {
        }

        /* Do not process the results if the request was cancelled */
        if (req.isCancelled() || req.isFailed()) {
            return false;
        }

        /* The request will fill 'syscalls' */
        fSystemCalls = syscalls;

        /* Serialize the collections to disk for future usage */
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(file))) {
            oos.writeObject(syscalls);
        } catch (IOException e) {
            /* Didn't work, oh well. We will just re-read the trace next time */
        }

        for (LatencyAnalysisListener listener : fListeners) {
            listener.onComplete(this, syscalls);
        }

        return true;
    }

    @Override
    protected void canceling() {
        ITmfEventRequest req = fOngoingRequest;
        if ((req != null) && (!req.isCompleted())) {
            req.cancel();
        }
    }

    /**
     * @return Results from the analysis in a ISegmentStore
     */
    public @Nullable ISegmentStore<ISegment> getResults() {
        return fSystemCalls;
    }

    private static class LatencyAnalysisRequest extends TmfEventRequest {

        private final IKernelAnalysisEventLayout fLayout;
        private final ISegmentStore<ISegment> fFullSyscalls;
        private final Map<Integer, SystemCall.InitialInfo> fOngoingSystemCalls = new HashMap<>();

        public LatencyAnalysisRequest(IKernelAnalysisEventLayout layout, ISegmentStore<ISegment> syscalls) {
            super(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
            fLayout = layout;
            /*
             * We do NOT make a copy here! We want to modify the list that was
             * passed in parameter.
             */
            fFullSyscalls = syscalls;
        }

        @Override
        public void handleData(final ITmfEvent event) {
            super.handleData(event);
            final String eventName = event.getType().getName();

            if (eventName.startsWith(fLayout.eventSyscallEntryPrefix()) ||
                    eventName.startsWith(fLayout.eventCompatSyscallEntryPrefix())) {
                /* This is a system call entry event */

                Integer tid = KernelTidAspect.INSTANCE.resolve(event);
                if (tid == null) {
                    // no information on this event/trace ?
                    return;
                }

                /* Record the event's data into the intial system call info */
                // String syscallName = fLayout.getSyscallNameFromEvent(event);
                long startTime = event.getTimestamp().getValue();
                String syscallName = eventName.substring(fLayout.eventSyscallEntryPrefix().length());
                FluentIterable<String> argNames = FluentIterable.from(event.getContent().getFieldNames());
                Map<String, String> args = argNames.toMap(new Function<String, String>() {
                    @Override
                    public String apply(@Nullable String input) {
                        return checkNotNull(event.getContent().getField(input).getValue().toString());
                    }
                });
                SystemCall.InitialInfo newSysCall = new SystemCall.InitialInfo(startTime, NonNullUtils.checkNotNull(syscallName), NonNullUtils.checkNotNull(args));
                fOngoingSystemCalls.put(tid, newSysCall);

            } else if (eventName.startsWith(fLayout.eventSyscallExitPrefix())) {
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
                fFullSyscalls.add(syscall);
            }
        }

        @Override
        public void handleCompleted() {
            fOngoingSystemCalls.clear();
        }
    }

}
