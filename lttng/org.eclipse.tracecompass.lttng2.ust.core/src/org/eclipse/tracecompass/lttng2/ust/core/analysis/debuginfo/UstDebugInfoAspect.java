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
import org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.debuginfo.UstDebugInfoBinaryFile;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.tracecompass.lttng2.ust.core.trace.layout.ILttngUstEventLayout;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.lookup.TmfCallsite;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * Event aspect of UST traces to generate a {@link TmfCallsite} using the debug
 * info analysis and the IP (instruction pointer) context.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public class UstDebugInfoAspect implements ITmfEventAspect {

    /** Singleton instance */
    public static final UstDebugInfoAspect INSTANCE = new UstDebugInfoAspect();

    private UstDebugInfoAspect() {}

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
    public @Nullable String resolve(ITmfEvent event) {
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
        UstDebugInfoBinaryFile file = module.getMatchingFile(ts, vpid, ip);

        return (file == null ? null : file.toString());
    }

}
