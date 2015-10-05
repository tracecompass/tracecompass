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
import org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.debuginfo.UstDebugInfoLoadedBinaryFile;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.tracecompass.lttng2.ust.core.trace.layout.ILttngUstEventLayout;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.lookup.TmfCallsite;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

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
        return nullToEmptyString(Messages.UstDebugInfoAnalysis_AspectName);
    }

    @Override
    public String getHelpText() {
        return nullToEmptyString(Messages.UstDebugInfoAnalysis_AspectHelpText);
    }

    // TODO Will return a TmfCallsite eventually
    @Override
    public @Nullable TmfCallsite resolve(ITmfEvent event) {
        /* This aspect only supports UST traces */
        if (!(event.getTrace() instanceof LttngUstTrace)) {
            return null;
        }

        ILttngUstEventLayout layout = ((LttngUstTrace) event.getTrace()).getEventLayout();

        /* We need both the vpid and ip contexts */
        ITmfEventField vpidField = event.getContent().getField(layout.contextVpid());
        ITmfEventField ipField = event.getContent().getField(layout.contextIp());
        if (vpidField == null || ipField == null) {
            return null;
        }
        Long vpid = (Long) vpidField.getValue();
        Long ip = (Long) ipField.getValue();

        /*
         * First match the IP to the correct binary or library, by using the
         * UstDebugInfoAnalysis.
         */
        UstDebugInfoAnalysisModule module =
                TmfTraceUtils.getAnalysisModuleOfClass(event.getTrace(),
                        UstDebugInfoAnalysisModule.class, UstDebugInfoAnalysisModule.ID);
        if (module == null) {
            /*
             * The analysis is not available for this trace, we won't be
             * able to find the information.
             */
            return null;
        }
        long ts = event.getTimestamp().getValue();
        UstDebugInfoLoadedBinaryFile file = module.getMatchingFile(ts, vpid, ip);
        if (file == null) {
            return null;
        }

        long offset;
        if (isMainBinary(file)) {
            /*
             * In the case of the object being the main binary (loaded at a very
             * low address), we must pass the actual ip address to addr2line.
             */
            offset = ip.longValue();
        } else {
            offset = (ip.longValue() - file.getBaseAddress());
        }

        if (offset < 0) {
            throw new IllegalStateException();
        }

        Iterable<TmfCallsite> callsites = FileOffsetMapper.getCallsiteFromOffset(new File(file.getFilePath()), offset);

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

    private static boolean isMainBinary(UstDebugInfoLoadedBinaryFile file) {
        /*
         * Ghetto binary/library identification for now. It would be possible to
         * parse the ELF binary to check if it is position-independent
         * (-fPIC/-fPIE) or not.
         */
        return (!file.getFilePath().endsWith(".so")); //$NON-NLS-1$
    }

}
