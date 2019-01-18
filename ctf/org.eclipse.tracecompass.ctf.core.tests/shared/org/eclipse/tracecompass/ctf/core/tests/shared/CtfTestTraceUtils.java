/*******************************************************************************
 * Copyright (c) 2013, 2018 Ericsson, EfficiOS Inc. and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.shared;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;

/**
 * Wrapper for the CTF test traces, instantiating {@link CTFTrace} objects from
 * them.
 *
 * @author Alexandre Montplaisir
 */
public final class CtfTestTraceUtils {

    private CtfTestTraceUtils() {}

    /**
     * Get a CTFTrace instance of a test trace.
     *
     * @param trace
     *            The test trace to use
     * @return The CTFTrace object
     * @throws CTFException
     *             If there is an error initializing the trace
     */
    public static synchronized CTFTrace getTrace(CtfTestTrace trace) throws CTFException {
        String tracePath;
        try {
            tracePath = FileUtils.toFile(FileLocator.toFileURL(trace.getTraceURL())).getAbsolutePath();
        } catch (IOException e) {
            throw new IllegalStateException();
        }

        return new CTFTrace(tracePath);
    }
}
