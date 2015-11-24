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

import com.google.common.collect.Iterables;

/**
 * Event aspect of UST traces to generate a {@link TmfCallsite} using the debug
 * info analysis and the IP (instruction pointer) context.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public class UstDebugInfoSourceAspect implements ITmfEventAspect {

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

    @Override
    public @Nullable TmfCallsite resolve(ITmfEvent event) {
        /* This aspect only supports UST traces */
        if (!(event.getTrace() instanceof LttngUstTrace)) {
            return null;
        }

        /*
         * Resolve the binary callsite first, from there we can use the file's
         * debug information if it is present.
         */
        BinaryCallsite bc = UstDebugInfoBinaryAspect.INSTANCE.resolve(event);
        if (bc == null) {
            return null;
        }

        Iterable<TmfCallsite> callsites = FileOffsetMapper.getCallsiteFromOffset(new File(bc.getBinaryFilePath()), bc.getOffset());

        if (callsites == null || Iterables.isEmpty(callsites)) {
            return null;
        }
        /*
         * TMF only supports the notion of one callsite per event at the moment.
         * We will take the "deepest" one in the stack, which should refer to
         * the initial, non-inlined location.
         */
        return Iterables.getLast(callsites);
    }
}
