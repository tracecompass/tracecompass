/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.ust.core.analysis.debuginfo;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.OptionalLong;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.debuginfo.UstDebugInfoStateProvider;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAbstractAnalysisRequirement;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

/**
 * Analysis to provide TMF Callsite information by mapping IP (instruction
 * pointer) contexts to address/line numbers via debug information.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public class UstDebugInfoAnalysisModule extends TmfStateSystemAnalysisModule {

    /**
     * Analysis ID, it should match that in the plugin.xml file
     */
    public static final String ID = "org.eclipse.linuxtools.lttng2.ust.analysis.debuginfo"; //$NON-NLS-1$

    @Override
    protected ITmfStateProvider createStateProvider() {
        return new UstDebugInfoStateProvider(checkNotNull(getTrace()));
    }

    @Override
    public boolean setTrace(ITmfTrace trace) throws TmfAnalysisException {
        if (!(trace instanceof LttngUstTrace)) {
            return false;
        }
        return super.setTrace(trace);
    }

    @Override
    protected @Nullable LttngUstTrace getTrace() {
        return (LttngUstTrace) super.getTrace();
    }

    @Override
    public Iterable<TmfAbstractAnalysisRequirement> getAnalysisRequirements() {
        // TODO specify actual requirements once the requirement-checking is
        // implemented. This analysis needs "ip" and "vpid" contexts.
        return Collections.EMPTY_SET;
    }

    @Override
    public boolean canExecute(ITmfTrace trace) {
        /* The analysis can only work with LTTng-UST traces... */
        if (!(trace instanceof LttngUstTrace)) {
            return false;
        }
        LttngUstTrace ustTrace = (LttngUstTrace) trace;
        String tracerName = CtfUtils.getTracerName(ustTrace);
        int majorVersion = CtfUtils.getTracerMajorVersion(ustTrace);
        int minorVersion = CtfUtils.getTracerMinorVersion(ustTrace);

        /* ... taken with UST >= 2.8 ... */
        if (!LttngUstTrace.TRACER_NAME.equals(tracerName)) {
            return false;
        }
        if (majorVersion < 2) {
            return false;
        }
        if (majorVersion == 2 && minorVersion < 8) {
            return false;
        }

        /* ... that respect the ip/vpid contexts requirements. */
        return super.canExecute(trace);
    }

    // ------------------------------------------------------------------------
    // Class-specific operations
    // ------------------------------------------------------------------------

    /**
     * Return all the binaries that were detected in the trace.
     *
     * @return The binaries (executables or libraries) referred to in the trace.
     */
    public Collection<UstDebugInfoBinaryFile> getAllBinaries() {
        waitForCompletion();
        ITmfStateSystem ss = getStateSystem();
        if (ss == null) {
            /* State system might not yet be initialized */
            return Collections.EMPTY_SET;
        }

        final @NonNull Set<UstDebugInfoBinaryFile> files = new TreeSet<>();
        ImmutableList.Builder<Integer> builder = ImmutableList.builder();
        List<Integer> vpidQuarks = ss.getSubAttributes(ITmfStateSystem.ROOT_ATTRIBUTE, false);
        for (Integer vpidQuark : vpidQuarks) {
            builder.addAll(ss.getSubAttributes(vpidQuark, false));
        }
        List<Integer> baddrQuarks = builder.build();

        try {
            for (Integer baddrQuark : baddrQuarks) {
                int buildIdQuark = ss.getQuarkRelative(baddrQuark, UstDebugInfoStateProvider.BUILD_ID_ATTRIB);
                int debugLinkQuark = ss.getQuarkRelative(baddrQuark, UstDebugInfoStateProvider.DEBUG_LINK_ATTRIB);
                int pathQuark = ss.getQuarkRelative(baddrQuark, UstDebugInfoStateProvider.PATH_ATTRIB);
                int isPICQuark = ss.getQuarkRelative(baddrQuark, UstDebugInfoStateProvider.IS_PIC_ATTRIB);
                long ts = ss.getStartTime();

                /*
                 * Iterate over each mapping there ever was at this base
                 * address.
                 */
                ITmfStateInterval interval = StateSystemUtils.queryUntilNonNullValue(ss, baddrQuark, ts, Long.MAX_VALUE);
                while (interval != null) {
                    ts = interval.getStartTime();

                    ITmfStateValue filePathStateValue = ss.querySingleState(ts, pathQuark).getStateValue();
                    String filePath = filePathStateValue.unboxStr();

                    ITmfStateValue buildIdStateValue = ss.querySingleState(ts, buildIdQuark).getStateValue();
                    String buildId = unboxStrOrNull(buildIdStateValue);

                    ITmfStateValue debuglinkStateValue = ss.querySingleState(ts, debugLinkQuark).getStateValue();
                    String debugLink = unboxStrOrNull(debuglinkStateValue);

                    ITmfStateValue isPICStateValue = ss.querySingleState(ts, isPICQuark).getStateValue();
                    Boolean isPIC = isPICStateValue.unboxInt() != 0;

                    files.add(new UstDebugInfoBinaryFile(filePath, buildId, debugLink, isPIC));

                    /*
                     * Go one past the end of the interval, and perform the
                     * query again to find the next mapping at this address.
                     */
                    ts = interval.getEndTime() + 1;
                    interval = StateSystemUtils.queryUntilNonNullValue(ss, baddrQuark, ts, Long.MAX_VALUE);
                }
            }
        } catch (AttributeNotFoundException e) {
            throw new IllegalStateException(e);
        } catch (TimeRangeException | StateSystemDisposedException e) {
            /* Oh well, such is life. */
        }
        return files;
    }

    /**
     * Get the binary file (executable or library) that corresponds to a given
     * instruction pointer, at a given time.
     *
     * @param ts
     *            The timestamp
     * @param vpid
     *            The VPID of the process we are querying for
     * @param ip
     *            The instruction pointer of the trace event. Normally comes
     *            from a 'ip' context.
     * @return A {@link UstDebugInfoLoadedBinaryFile} object, describing the
     *         binary file and its base address.
     * @noreference Meant to be used internally by
     *              {@link UstDebugInfoBinaryAspect} only.
     */
    @VisibleForTesting
    public @Nullable UstDebugInfoLoadedBinaryFile getMatchingFile(long ts, long vpid, long ip) {
        try {
            waitForCompletion();
            final ITmfStateSystem ss = getStateSystem();
            if (ss == null) {
                /* State system might not yet be initialized */
                return null;
            }

            List<Integer> possibleBaddrQuarks = ss.getQuarks(String.valueOf(vpid), "*"); //$NON-NLS-1$
            List<ITmfStateInterval> state = ss.queryFullState(ts);

            /* Get the most probable base address from all the known ones */
            OptionalLong potentialBaddr = possibleBaddrQuarks.stream()
                    .filter(quark -> {
                        /* Keep only currently (at ts) mapped objects. */
                        ITmfStateValue value = state.get(quark).getStateValue();
                        return value.getType() == ITmfStateValue.Type.INTEGER && value.unboxInt() == 1;
                    })
                    .map(quark -> ss.getAttributeName(quark.intValue()))
                    .mapToLong(baddrStr -> Long.parseLong(baddrStr))
                    .filter(baddr -> baddr <= ip)
                    .max();

            if (!potentialBaddr.isPresent()) {
                return null;
            }

            long baddr = potentialBaddr.getAsLong();
            final int baddrQuark = ss.getQuarkAbsolute(String.valueOf(vpid),
                                                       String.valueOf(baddr));

            final int memszQuark = ss.getQuarkRelative(baddrQuark, UstDebugInfoStateProvider.MEMSZ_ATTRIB);
            final long memsz = state.get(memszQuark).getStateValue().unboxLong();

            /* Make sure the 'ip' fits the range of this object. */
            if (!(ip < baddr + memsz)) {
                /*
                 * Not the correct memory range after all. We do not have
                 * information about the library that was loaded here.
                 */
                return null;
            }

            final int pathQuark = ss.getQuarkRelative(baddrQuark, UstDebugInfoStateProvider.PATH_ATTRIB);
            String filePath = state.get(pathQuark).getStateValue().unboxStr();

            final int buildIdQuark = ss.getQuarkRelative(baddrQuark, UstDebugInfoStateProvider.BUILD_ID_ATTRIB);
            ITmfStateValue buildIdValue = state.get(buildIdQuark).getStateValue();
            String buildId = unboxStrOrNull(buildIdValue);

            final int debugLinkQuark = ss.getQuarkRelative(baddrQuark, UstDebugInfoStateProvider.DEBUG_LINK_ATTRIB);
            ITmfStateValue debugLinkValue = state.get(debugLinkQuark).getStateValue();
            String debugLink = unboxStrOrNull(debugLinkValue);

            final int isPicQuark = ss.getQuarkRelative(baddrQuark, UstDebugInfoStateProvider.IS_PIC_ATTRIB);
            boolean isPic = state.get(isPicQuark).getStateValue().unboxInt() != 0;

            return new UstDebugInfoLoadedBinaryFile(baddr, filePath, buildId, debugLink, isPic);

        } catch (AttributeNotFoundException e) {
            // TODO: that's probably not true anymore
            /* We're only using quarks we've checked for. */
            throw new IllegalStateException(e);
        } catch (TimeRangeException | StateSystemDisposedException e) {
            return null;
        }
    }

    private static @Nullable String unboxStrOrNull(ITmfStateValue value) {
        return (value.isNull() ? null : value.unboxStr());
    }
}
