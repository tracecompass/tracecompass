/*******************************************************************************
 * Copyright (c) 2014, 2019 École Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *   Guilliano Molaire - Provide the requirements of the analysis
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.memory;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.model.OsStrings;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.IAnalysisProgressListener;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.memory.UstMemoryStateProvider.MemoryAllocation;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.tracecompass.lttng2.ust.core.trace.layout.ILttngUstEventLayout;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.SegmentStoreFactory;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAbstractAnalysisRequirement;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAbstractAnalysisRequirement.PriorityLevel;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAnalysisEventRequirement;
import org.eclipse.tracecompass.tmf.core.segment.ISegmentAspect;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;

import com.google.common.collect.ImmutableSet;

/**
 * This analysis build a state system from the libc memory instrumentation on a
 * UST trace
 *
 * @author Geneviève Bastien
 */
public class UstMemoryAnalysisModule extends TmfStateSystemAnalysisModule implements ISegmentStoreProvider {

    /**
     * Analysis ID, it should match that in the plugin.xml file
     */
    public static final @NonNull String ID = "org.eclipse.linuxtools.lttng2.ust.analysis.memory"; //$NON-NLS-1$

    /** The analysis's requirements. Only set after the trace is set. */
    private @Nullable Set<TmfAbstractAnalysisRequirement> fAnalysisRequirements;

    private final ListenerList<IAnalysisProgressListener> fListeners = new ListenerList<>(ListenerList.IDENTITY);

    private @Nullable ISegmentStore<@NonNull ISegment> fSegmentStore = null;

    private Map<Long, MemoryAllocation> fPotentialLeaks;

    private static final class PotentialLeakTidAspect implements ISegmentAspect {
        public static final ISegmentAspect INSTANCE = new PotentialLeakTidAspect();

        private PotentialLeakTidAspect() { }

        @Override
        public String getHelpText() {
            return checkNotNull(Messages.SegmentAspectHelpText_PotentialLeakTid);
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
            if (segment instanceof PotentialLeakSegment) {
                return ((PotentialLeakSegment) segment).getTid();
            }
            return -1;
        }
    }

    @Override
    public ITmfStateProvider createStateProvider() {
        return new UstMemoryStateProvider(checkNotNull(getTrace()), this);
    }

    @Override
    protected boolean executeAnalysis(IProgressMonitor monitor) {
        if (!super.executeAnalysis(monitor)) {
            return false;
        }

        ITmfTrace trace = checkNotNull(getTrace());
        String segmentFileName = getSsFileName() + ".seg"; //$NON-NLS-1$
        /* See if the data file already exists on disk */
        String dir = TmfTraceManager.getSupplementaryFileDir(trace);
        final Path file = Paths.get(dir, segmentFileName);

        boolean needsBuilding = true;

        Map<Long, MemoryAllocation> unfreedMemory = fPotentialLeaks;
        if (unfreedMemory == null || unfreedMemory.isEmpty()) {
            needsBuilding = false;
        }

        ISegmentStore<@NonNull ISegment> segmentStore = null;
        try {
            if (needsBuilding) {
                Files.deleteIfExists(file);
            }
            segmentStore = SegmentStoreFactory.createOnDiskSegmentStore(file, PotentialLeakSegment.MEMORY_SEGMENT_READ_FACTORY, 1);
        } catch (IOException e) {
            return false;
        }
        if (needsBuilding) {
            fillStore(segmentStore, unfreedMemory);
        }

        fSegmentStore = segmentStore;
        sendUpdate(segmentStore);
        return true;
    }

    private static void fillStore(ISegmentStore<@NonNull ISegment> segmentStore, Map<Long, MemoryAllocation> unfreedMemory) {
        for (Entry<Long, MemoryAllocation> unfreedMem : unfreedMemory.entrySet()) {
            MemoryAllocation memAlloc = unfreedMem.getValue();
            segmentStore.add(new PotentialLeakSegment(memAlloc.getTs(), memAlloc.getTs(), memAlloc.getTid()));
        }
        segmentStore.close(false);
    }

    @Override
    public Iterable<TmfAbstractAnalysisRequirement> getAnalysisRequirements() {
        Set<TmfAbstractAnalysisRequirement> requirements = fAnalysisRequirements;
        if (requirements == null) {
            ITmfTrace trace = getTrace();
            ILttngUstEventLayout layout;
            if (!(trace instanceof LttngUstTrace)) {
                layout = ILttngUstEventLayout.DEFAULT_LAYOUT;
            } else {
                layout = ((LttngUstTrace) trace).getEventLayout();
            }

            @NonNull Set<@NonNull String> requiredEvents = ImmutableSet.of(
                    layout.eventLibcMalloc(),
                    layout.eventLibcFree(),
                    layout.eventLibcCalloc(),
                    layout.eventLibcRealloc(),
                    layout.eventLibcMemalign(),
                    layout.eventLibcPosixMemalign()
                  );

            /* Initialize the requirements for the analysis: domain and events */
            TmfAbstractAnalysisRequirement eventsReq = new TmfAnalysisEventRequirement(requiredEvents, PriorityLevel.MANDATORY);
            /*
             * In order to have these events, the libc wrapper with probes should be
             * loaded
             */
            eventsReq.addInformation(nullToEmptyString(Messages.UstMemoryAnalysisModule_EventsLoadingInformation));
            eventsReq.addInformation(nullToEmptyString(Messages.UstMemoryAnalysisModule_EventsLoadingExampleInformation));

            /* The domain type of the analysis */
            // FIXME: The domain requirement should have a way to be verified. It is useless otherwise
            // TmfAnalysisRequirement domainReq = new TmfAnalysisRequirement(nullToEmptyString(SessionConfigStrings.CONFIG_ELEMENT_DOMAIN));
            // domainReq.addValue(nullToEmptyString(SessionConfigStrings.CONFIG_DOMAIN_TYPE_UST), ValuePriorityLevel.MANDATORY);

            requirements = ImmutableSet.of(eventsReq);
            fAnalysisRequirements = requirements;
        }
        return requirements;
    }

    @Override
    public void addListener(@NonNull IAnalysisProgressListener listener) {
        fListeners.add(listener);
    }

    @Override
    public void removeListener(@NonNull IAnalysisProgressListener listener) {
        fListeners.remove(listener);
    }

    /**
     * Send the segment store to all its listener
     *
     * @param store
     *            The segment store to broadcast
     */
    private void sendUpdate(@NonNull ISegmentStore<@NonNull ISegment> store) {
        for (Object listener : fListeners) {
            ((IAnalysisProgressListener) listener).onComplete(this, store);
        }
    }

    @Override
    public Iterable<ISegmentAspect> getSegmentAspects() {
        return Collections.singletonList(PotentialLeakTidAspect.INSTANCE);
    }

    @Override
    public @Nullable ISegmentStore<@NonNull ISegment> getSegmentStore() {
        return fSegmentStore;
    }

    /**
     * Set the potential leaks that are left after the analysis is finished
     *
     * @param memory
     *            The map of potential leaks
     */
    public void setPotentialLeaks(Map<Long, MemoryAllocation> memory) {
        fPotentialLeaks = memory;
    }

    @Override
    public void dispose() {
        super.dispose();
        ISegmentStore<@NonNull ISegment> store = fSegmentStore;
        if (store != null) {
            store.dispose();
        }
    }
}
