/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.ust.core.analysis.debuginfo;

import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.tracecompass.lttng2.ust.core.trace.layout.ILttngUstEventLayout;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Event aspect of UST traces that indicate the binary callsite (binary, symbol
 * and offset) from an IP (instruction pointer) context.
 *
 * Unlike the {@link UstDebugInfoSourceAspect}, this information should be
 * available even without debug information.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public class UstDebugInfoBinaryAspect implements ITmfEventAspect<BinaryCallsite> {

    /** Singleton instance */
    public static final UstDebugInfoBinaryAspect INSTANCE = new UstDebugInfoBinaryAspect();
    private static final long CACHE_SIZE = 1000;

    /**
     * Cache of all calls to 'addr2line', so that we can avoid recalling the
     * external process repeatedly.
     *
     * It is static, meaning one cache for the whole application, since the
     * symbols in a file on disk are independent from the trace referring to it.
     */
    private static final Optional<BinaryCallsite> OPTIONAL_NULL = Objects.requireNonNull(Optional.empty());
    private static final LoadingCache<TraceBinarySymbol, Optional<BinaryCallsite>> BINARY_CALLSITE_CACHE;
    static {
        BINARY_CALLSITE_CACHE = Objects.requireNonNull(CacheBuilder.newBuilder()
                .maximumSize(CACHE_SIZE)
                .build(new CacheLoader<TraceBinarySymbol, Optional<BinaryCallsite>>() {
                    @Override
                    public Optional<BinaryCallsite> load(TraceBinarySymbol symbolIp) {
                        /*
                         * First match the IP to the correct binary or library, by using the
                         * UstDebugInfoAnalysis.
                         */
                        UstDebugInfoAnalysisModule module = TmfTraceUtils.getAnalysisModuleOfClass(symbolIp.fTrace,
                                UstDebugInfoAnalysisModule.class, UstDebugInfoAnalysisModule.ID);
                        if (module == null) {
                            /*
                             * The analysis is not available for this trace, we won't be able to
                             * find the information.
                             */
                            return OPTIONAL_NULL;
                        }
                        UstDebugInfoLoadedBinaryFile file = module.getMatchingFile(symbolIp.fTs, symbolIp.fPid, symbolIp.fIp);
                        if (file == null) {
                            return OPTIONAL_NULL;
                        }

                        /* Apply the path prefix defined by the trace, if any */

                        String fullPath = new File(symbolIp.fTrace.getSymbolProviderConfig().getActualRootDirPath(), file.getFilePath()).toString();

                        long offset;
                        if (file.isPic()) {
                            offset = symbolIp.fIp - file.getBaseAddress();
                        } else {
                            /*
                             * In the case of the object being non-position-independent, we must
                             * pass the actual 'ip' address directly to addr2line.
                             */
                            offset = symbolIp.fIp;
                        }

                        return Objects.requireNonNull(Optional.of(new BinaryCallsite(fullPath, file.getBuildId(), offset, file.isPic(), file.getValidityStart(), file.getValidityEnd())));
                    }
                }));
    }

    private static final class TraceBinarySymbol {

        private LttngUstTrace fTrace;
        private int fPid;
        private long fTs;
        private long fIp;

        public TraceBinarySymbol(LttngUstTrace trace, int pid, long ts, long ip) {
            fTrace = trace;
            fPid = pid;
            fTs = ts;
            fIp = ip;
        }

        @Override
        public int hashCode() {
            // The timestamp does not participate in the hash code, it is for
            // the purpose of the cache
            return Objects.hash(fTrace, fPid, fIp);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            // The timestamp does not participate in the equality, it is for the
            // purpose of the cache
            if (!(obj instanceof TraceBinarySymbol)) {
                return false;
            }
            TraceBinarySymbol other = (TraceBinarySymbol) obj;
            return Objects.equals(fTrace, other.fTrace) && fPid == other.fPid && fIp == other.fIp;
        }

    }

    private UstDebugInfoBinaryAspect() {
      // Do nothing
    }

    @Override
    public String getName() {
        return nullToEmptyString(Messages.UstDebugInfoAnalysis_BinaryAspectName);
    }

    @Override
    public String getHelpText() {
        return nullToEmptyString(Messages.UstDebugInfoAnalysis_BinaryAspectHelpText);
    }

    @Override
    public @Nullable BinaryCallsite resolve(ITmfEvent event) {
        /* This aspect only supports UST traces */
        if (!(event.getTrace() instanceof LttngUstTrace)) {
            return null;
        }
        LttngUstTrace trace = (LttngUstTrace) event.getTrace();

        ILttngUstEventLayout layout = trace.getEventLayout();

        /* We need both the vpid and ip contexts */
        ITmfEventField vpidField = event.getContent().getField(layout.contextVpid());
        ITmfEventField ipField = event.getContent().getField(layout.contextIp());
        if (ipField == null) {
            ipField = event.getContent().getField(layout.fieldAddr());
        }
        if (vpidField == null || ipField == null) {
            return null;
        }
        Long vpid = (Long) vpidField.getValue();
        Long ip = (Long) ipField.getValue();
        long ts = event.getTimestamp().toNanos();

        return getBinaryCallsite(trace, vpid.intValue(), ts, ip.longValue());
    }

    /**
     * Get the binary callsite (which means binary file and offset in this file)
     * corresponding to the given instruction pointer, for the given PID and
     * timetamp.
     *
     * @param trace
     *            The trace, from which we will get the debug info analysis
     * @param pid
     *            The PID for which we want the symbol
     * @param ts
     *            The timestamp of the query
     * @param ip
     *            The instruction pointer address
     * @return The {@link BinaryCallsite} object with the relevant information
     */
    public static @Nullable BinaryCallsite getBinaryCallsite(LttngUstTrace trace, int pid, long ts, long ip) {
        TraceBinarySymbol traceBinarySymbol = new TraceBinarySymbol(trace, pid, ts, ip);
        Optional<BinaryCallsite> binaryCallsite = BINARY_CALLSITE_CACHE.getUnchecked(traceBinarySymbol);
        if (!binaryCallsite.isPresent()) {
            return null;
        }
        if (!binaryCallsite.get().intersects(ts)) {
            BINARY_CALLSITE_CACHE.invalidate(traceBinarySymbol);
            binaryCallsite = BINARY_CALLSITE_CACHE.getUnchecked(traceBinarySymbol);
        }
        return (binaryCallsite.isPresent() ? binaryCallsite.get() : null);
    }

    /**
     * Invalidate all symbols in the cache. This should be called when UST debug
     * info configuration changes.
     *
     * @since 4.1
     */
    public static void invalidateSymbolCache() {
        // Invalidate all symbols in the cache, even if only one trace has
        // changed. It won't happen very often, so it's not a very big
        // performance hit compared to the performance gain of the cache
        BINARY_CALLSITE_CACHE.invalidateAll();
    }
}
