/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
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
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;

/**
 * Aspect for the function location obtained with the UST debug info.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public class UstDebugInfoFunctionAspect implements ITmfEventAspect<FunctionLocation> {

    /** Singleton instance */
    public static final UstDebugInfoFunctionAspect INSTANCE = new UstDebugInfoFunctionAspect();

    private UstDebugInfoFunctionAspect() {}

    @Override
    public String getName() {
        return nullToEmptyString(Messages.UstDebugInfoAnalysis_FunctionAspectName);
    }

    @Override
    public String getHelpText() {
        return nullToEmptyString(Messages.UstDebugInfoAnalysis_FunctionAspectHelpText);
    }

    @Override
    public @Nullable FunctionLocation resolve(ITmfEvent event) {
        /* Resolve the binary callsite first. */
        BinaryCallsite bc = UstDebugInfoBinaryAspect.INSTANCE.resolve(event);
        if (bc == null) {
            return null;
        }

        return getFunctionFromBinaryLocation(bc);
    }

    /**
     * Get a function location starting directly from a binary callsite, instead
     * of from a trace event.
     *
     * @param bc
     *            The binary callsite, representing a binary and offset within
     *            this binary
     * @return The corresponding function location
     * @since 2.1
     */
    public static @Nullable FunctionLocation getFunctionFromBinaryLocation(BinaryCallsite bc) {
        String functionName = FileOffsetMapper.getFunctionNameFromOffset(
                new File(bc.getBinaryFilePath()),
                bc.getBuildId(),
                bc.getOffset());
        if (functionName == null) {
            return null;
        }

        /* We do not track the offset inside the function at this time */
        return new FunctionLocation(functionName, null);
    }

}
