/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.lttng.kernel.core.tests.shared.vm;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.trace.TmfXmlKernelTraceStub;
import org.eclipse.tracecompass.lttng2.kernel.core.tests.Activator;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * List the available virtual machine host and guest traces
 *
 * @author Geneviève Bastien
 */
public enum VmTraces {

    /** Host from simple QEMU/KVM experiment */
    HOST_ONE_QEMUKVM("vm/OneQemuKvm/host.xml"),
    /** Guest from simple QEMU/KVM experiment */
    GUEST_ONE_QEMUKVM("vm/OneQemuKvm/guest.xml");

    private static final @NonNull String filePath = "testfiles";

    private final IPath fPath;

    VmTraces(String path) {
        IPath relativePath = new Path(filePath + File.separator + path);
        Activator plugin = Activator.getDefault();
        if (plugin == null) {
            /*
             * Shouldn't happen but at least throw something to get the test to
             * fail early
             */
            throw new IllegalStateException();
        }
        URL location = FileLocator.find(plugin.getBundle(), relativePath, null);
        try {
            fPath = new Path(FileLocator.toFileURL(location).getPath());
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }

    /**
     * Return a TmfXmlTraceStub object of this test trace. It will be already
     * initTrace()'ed.
     *
     * Make sure you call {@link #exists()} before calling this!
     *
     * @return A TmfXmlTraceStub reference to this trace
     */
    public @Nullable ITmfTrace getTrace() {
        ITmfTrace trace = new TmfXmlKernelTraceStub();
        IStatus status = trace.validate(null, fPath.toOSString());
        if (!status.isOK()) {
            return null;
        }
        try {
            trace.initTrace(null, fPath.toOSString(), TmfEvent.class);
        } catch (TmfTraceException e) {
            return null;
        }
        return trace;
    }

    /**
     * Check if the trace actually exists on disk or not.
     *
     * @return If the trace is present
     */
    public boolean exists() {
        return fPath.toFile().exists();
    }
}
