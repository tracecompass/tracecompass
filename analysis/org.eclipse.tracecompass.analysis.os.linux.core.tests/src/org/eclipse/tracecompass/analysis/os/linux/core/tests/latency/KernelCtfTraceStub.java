/*******************************************************************************
 * Copyright (c) 2016, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.tests.latency;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.trace.KernelEventLayoutStub;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelTrace;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;

class KernelCtfTraceStub extends CtfTmfTrace implements IKernelTrace {
    public KernelCtfTraceStub() {
        super();
    }

    public static synchronized KernelCtfTraceStub getTrace(CtfTestTrace ctfTrace) {
        String tracePath;
        try {
            tracePath = FileUtils.toFile(FileLocator.toFileURL(ctfTrace.getTraceURL())).getAbsolutePath();
        } catch (IOException e) {
            throw new IllegalStateException();
        }

        KernelCtfTraceStub trace = new KernelCtfTraceStub();
        try {
            trace.initTrace(null, tracePath, CtfTmfEvent.class);
        } catch (TmfTraceException e) {
            /* Should not happen if tracesExist() passed */
            throw new RuntimeException(e);
        }
        return trace;
    }

    @Override
    public @NonNull IKernelAnalysisEventLayout getKernelEventLayout() {
        return KernelEventLayoutStub.getInstance();
    }
}