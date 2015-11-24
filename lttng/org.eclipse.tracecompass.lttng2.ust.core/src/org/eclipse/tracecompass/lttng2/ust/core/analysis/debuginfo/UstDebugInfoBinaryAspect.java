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

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.debuginfo.UstDebugInfoLoadedBinaryFile;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.tracecompass.lttng2.ust.core.trace.layout.ILttngUstEventLayout;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

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
public class UstDebugInfoBinaryAspect implements ITmfEventAspect {

    /** Singleton instance */
    public static final UstDebugInfoBinaryAspect INSTANCE = new UstDebugInfoBinaryAspect();

    private UstDebugInfoBinaryAspect() {}

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
        if (isPIC(file)) {
            offset = (ip.longValue() - file.getBaseAddress());
        } else {
            /*
             * In the case of the object being the main binary (loaded at a very
             * low address), we must pass the actual ip address to addr2line.
             */
            offset = ip.longValue();
        }

        // TODO If the binary is present on the current file system, we could
        // try to get the symbol name from it.

        return new BinaryCallsite(file.getFilePath(), EMPTY_STRING, offset);
    }

    /**
     * Return if the given file (binary or library) is Position-Independent Code
     * or not. This indicates if addr2line considers the addresses as absolute
     * addresses or as offsets.
     */
    private static boolean isPIC(UstDebugInfoLoadedBinaryFile file) {
        /*
         * Ghetto binary/library identification for now. It would be possible to
         * parse the ELF binary to check if it is position-independent
         * (-fPIC/-fPIE) or not.
         */
        String filePath = file.getFilePath();
        return (filePath.endsWith(".so") || filePath.contains(".so.")); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
