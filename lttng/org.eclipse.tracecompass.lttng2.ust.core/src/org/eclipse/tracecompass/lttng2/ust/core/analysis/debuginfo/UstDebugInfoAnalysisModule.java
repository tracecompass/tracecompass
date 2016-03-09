/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir
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
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.debuginfo.UstDebugInfoBinaryFile;
import org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.debuginfo.UstDebugInfoLoadedBinaryFile;
import org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.debuginfo.UstDebugInfoStateProvider;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAnalysisRequirement;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfUtils;

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
    public Iterable<TmfAnalysisRequirement> getAnalysisRequirements() {
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
        ITmfStateSystem ss = checkNotNull(getStateSystem());

        Set<UstDebugInfoBinaryFile> files = new TreeSet<>();
        try {
            ImmutableList.Builder<Integer> builder = ImmutableList.builder();
            List<Integer> vpidQuarks = ss.getSubAttributes(-1, false);
            for (Integer vpidQuark : vpidQuarks) {
                builder.addAll(ss.getSubAttributes(vpidQuark, false));
            }
            List<Integer> baddrQuarks = builder.build();

            /*
             * For each "baddr" attribute, get the "buildId" sub-attribute,
             * whose value is the file path.
             */

            for (Integer baddrQuark : baddrQuarks) {

                List<Integer> buildIdQuarks = ss.getSubAttributes(baddrQuark, false);
                for (Integer buildIdQuark : buildIdQuarks) {
                    String buildId = ss.getAttributeName(buildIdQuark);

                    /*
                     * Explore the history of this attribute "horizontally",
                     * even though there should only be one valid interval.
                     */
                    ITmfStateInterval interval = StateSystemUtils.queryUntilNonNullValue(ss, buildIdQuark, ss.getStartTime(), Long.MAX_VALUE);
                    if (interval == null) {
                        /*
                         * If we created the attribute, we should have assigned
                         * a value to it!
                         */
                        throw new IllegalStateException();
                    }
                    String filePath = interval.getStateValue().unboxStr();

                    files.add(new UstDebugInfoBinaryFile(filePath, buildId));
                }
            }
        } catch (AttributeNotFoundException e) {
            throw new IllegalStateException(e);
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
     * @return The {@link UstDebugInfoBinaryFile} object, containing both the binary's path
     *         and its build ID.
     */
    @Nullable UstDebugInfoLoadedBinaryFile getMatchingFile(long ts, long vpid, long ip) {
        waitForCompletion();
        final ITmfStateSystem ss = checkNotNull(getStateSystem());

        List<Integer> possibleBaddrQuarks = ss.getQuarks(String.valueOf(vpid), "*"); //$NON-NLS-1$

        /* Get the most probable base address from all the known ones */
        NavigableSet<Long> possibleBaddrs = possibleBaddrQuarks.stream()
            .map(quark -> {
                String baddrStr = ss.getAttributeName(quark.intValue());
                return checkNotNull(Long.valueOf(baddrStr));
            })
            .collect(Collectors.toCollection(TreeSet::new));

        final Long potentialBaddr = possibleBaddrs.floor(ip);
        if (potentialBaddr == null) {
            return null;
        }

        /* Make sure the 'ip' fits in the expected memory range */
        try {
            final List<ITmfStateInterval> fullState = ss.queryFullState(ts);

            final int baddrQuark = ss.getQuarkAbsolute(String.valueOf(vpid), String.valueOf(potentialBaddr));
            final long endAddr = fullState.get(baddrQuark).getStateValue().unboxLong();

            if (!(ip < endAddr)) {
                /*
                 * Not the correct memory range after all. We do not have
                 * information about the library that was loaded here.
                 */
                return null;
            }

            /*
             * We've found the correct base address, now to determine what
             * library was loaded there at that time.
             */
            List<Integer> buildIds = ss.getSubAttributes(baddrQuark, false);
            Optional<Integer> potentialBuildIdQuark = buildIds.stream()
                .filter(id -> {
                    int quark = id.intValue();
                    ITmfStateValue value = fullState.get(quark).getStateValue();
                    return (!value.isNull());
                })
                .findFirst();

            if (!potentialBuildIdQuark.isPresent()) {
                /* We didn't have the information after all. */
                return null;
            }

            /* Ok, we have everything we need! Return the information. */
            long baddr = Long.parseLong(ss.getAttributeName(baddrQuark));

            int buildIdQuark = potentialBuildIdQuark.get().intValue();
            String buildId = ss.getAttributeName(buildIdQuark);
            String filePath = fullState.get(buildIdQuark).getStateValue().unboxStr();
            return new UstDebugInfoLoadedBinaryFile(baddr, filePath, buildId);

        } catch (AttributeNotFoundException e) {
            /* We're only using quarks we've checked for. */
            throw new IllegalStateException(e);
        } catch (StateSystemDisposedException e) {
            return null;
        }

    }
}
