/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.ust.core.analysis.debuginfo;

import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import java.io.File;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.debuginfo.FileOffsetMapper;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.lookup.TmfCallsite;

/**
 * Event aspect of UST traces to generate a {@link TmfCallsite} using the debug
 * info analysis and the IP (instruction pointer) context.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public class UstDebugInfoSourceAspect implements ITmfEventAspect<TmfCallsite> {

    /** Singleton instance */
    public static final UstDebugInfoSourceAspect INSTANCE = new UstDebugInfoSourceAspect();

    private UstDebugInfoSourceAspect() {}

    @Override
    public String getName() {
        return nullToEmptyString(Messages.UstDebugInfoAnalysis_SourceAspectName);
    }

    @Override
    public String getHelpText() {
        return nullToEmptyString(Messages.UstDebugInfoAnalysis_SourceAspectHelpText);
    }

    /**
     * @since 2.1
     */
    @Override
    public @Nullable TmfCallsite resolve(ITmfEvent event) {
        /* This aspect only supports UST traces */
        if (!(event.getTrace() instanceof LttngUstTrace)) {
            return null;
        }
        LttngUstTrace trace = (LttngUstTrace) event.getTrace();

        /*
         * Resolve the binary callsite first, from there we can use the file's
         * debug information if it is present.
         */
        BinaryCallsite bc = UstDebugInfoBinaryAspect.INSTANCE.resolve(event);
        if (bc == null) {
            return null;
        }

        TmfCallsite callsite = FileOffsetMapper.getCallsiteFromOffset(
                new File(bc.getBinaryFilePath()),
                bc.getBuildId(),
                bc.getOffset());
        if (callsite == null) {
            return null;
        }

        /*
         * Apply the path prefix again, this time on the path given from
         * addr2line. If applicable.
         */
        String pathPrefix = trace.getSymbolProviderConfig().getActualRootDirPath();
        if (pathPrefix.isEmpty()) {
            return callsite;
        }

        String fullFileName = (pathPrefix + callsite.getFileName());
        return new TmfCallsite(fullFileName, callsite.getLineNo());
    }

    /**
     * Get the source callsite (the full {@link TmfCallsite} information) from a
     * binary callsite.
     *
     * @param trace
     *            The trace, which may contain trace-specific configuration
     * @param bc
     *            The binary callsite
     * @return The source callsite, which sould include file name, function name
     *         and line number
     * @since 2.0
     * @deprecated Should not be needed anymore, call aspects's resolve() method
     *             directly. The SourceAspect does not include the function name
     *             anymore.
     */
    @Deprecated
    public static @Nullable SourceCallsite getSourceCallsite(LttngUstTrace trace, BinaryCallsite bc) {
        TmfCallsite callsite = FileOffsetMapper.getCallsiteFromOffset(
                new File(bc.getBinaryFilePath()),
                bc.getBuildId(),
                bc.getOffset());
        if (callsite == null) {
            return null;
        }

        Long callsiteLineNo = callsite.getLineNo();
        long lineNo = (callsiteLineNo == null ? -1 : callsiteLineNo.longValue());

        /*
         * Apply the path prefix again, this time on the path given from
         * addr2line. If applicable.
         */
        String pathPrefix = trace.getSymbolProviderConfig().getActualRootDirPath();
        if (pathPrefix.isEmpty()) {
            return new SourceCallsite(callsite.getFileName(), null, lineNo);
        }

        String fullFileName = (pathPrefix + callsite.getFileName());
        return new SourceCallsite(fullFileName, null, lineNo);
    }
}
