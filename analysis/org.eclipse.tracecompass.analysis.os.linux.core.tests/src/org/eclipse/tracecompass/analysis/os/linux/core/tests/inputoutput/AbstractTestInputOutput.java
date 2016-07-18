/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.tests.inputoutput;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.inputoutput.InputOutputAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelTidAspect;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.Activator;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.trace.KernelEventLayoutStub;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.trace.TmfXmlKernelTraceStub;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * Abstract for InputOuput test classes with utility methods to setup and tear
 * down the tests
 *
 * @author Geneviève Bastien
 */
public class AbstractTestInputOutput {

    private static final String IO_FILE_PATH = "testfiles/io_analysis/";

    private static class IOKernelEventLayout extends KernelEventLayoutStub {
        @Override
        public @NonNull String eventBlockRqMerge() {
            return "addons_elv_merge_requests";
        }

        @Override
        public @Nullable String eventStatedumpBlockDevice() {
            return "statedump_block_device";
        }

        @Override
        public @NonNull String eventSyscallEntryPrefix() {
            return "syscall_entry_";
        }

        @Override
        public @NonNull String eventCompatSyscallEntryPrefix() {
            return "syscall_compat_entry_";
        }

        @Override
        public @NonNull String eventSyscallExitPrefix() {
            return "syscall_exit_";
        }

        @Override
        public @NonNull String eventCompatSyscallExitPrefix() {
            return "syscall_compat_exit_";
        }
    }

    private static final @NonNull IKernelAnalysisEventLayout EVENT_LAYOUT = new IOKernelEventLayout();

    private ITmfTrace fTrace;

    private static void deleteSuppFiles(ITmfTrace trace) {
        /* Remove supplementary files */
        File suppDir = new File(TmfTraceManager.getSupplementaryFileDir(trace));
        for (File file : suppDir.listFiles()) {
            file.delete();
        }
    }

    /**
     * Constructor
     */
    public AbstractTestInputOutput() {

    }

    /**
     * Clean up
     */
    protected void deleteTrace() {
        ITmfTrace trace = fTrace;
        if (trace != null) {
            deleteSuppFiles(fTrace);
            fTrace.dispose();
        }
    }

    /**
     * Setup the trace for the tests and return the InputOutputAnalysisModule,
     * not executed.
     *
     * @param fileName
     *            The file name of the trace to open
     * @return The input output analysis module
     */
    protected @NonNull InputOutputAnalysisModule setUp(String fileName) {
        TmfXmlKernelTraceStub trace = new TmfXmlKernelTraceStub();
        trace.addEventAspect(KernelTidAspect.INSTANCE);
        trace.setKernelEventLayout(EVENT_LAYOUT);
        IPath filePath = Activator.getAbsoluteFilePath(IO_FILE_PATH + fileName);
        IStatus status = trace.validate(null, filePath.toOSString());
        if (!status.isOK()) {
            fail(status.getException().getMessage());
        }
        try {
            trace.initTrace(null, filePath.toOSString(), TmfEvent.class);
        } catch (TmfTraceException e) {
            fail(e.getMessage());
        }

        deleteSuppFiles(trace);
        ((TmfTrace) trace).traceOpened(new TmfTraceOpenedSignal(this, trace, null));
        fTrace = trace;

        /* Start the kernel analysis module */
        KernelAnalysisModule kernelMod = TmfTraceUtils.getAnalysisModuleOfClass(trace, KernelAnalysisModule.class, KernelAnalysisModule.ID);
        assertNotNull(kernelMod);
        kernelMod.schedule();
        kernelMod.waitForCompletion();

        InputOutputAnalysisModule module = TmfTraceUtils.getAnalysisModuleOfClass(trace, InputOutputAnalysisModule.class, InputOutputAnalysisModule.ID);
        assertNotNull(module);
        return module;
    }
}
