/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.signals;

import org.eclipse.tracecompass.analysis.os.linux.core.model.HostThread;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceModelSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * A signal to say a thread was selected
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
public class TmfThreadSelectedSignal extends TmfTraceModelSignal {

    private final HostThread fHostThread;

    /**
     * Constructor
     *
     * @param source
     *            the source
     * @param threadId
     *            the thread id (normally under 32768)
     * @param trace
     *            the trace
     */
    public TmfThreadSelectedSignal(Object source, int threadId, ITmfTrace trace) {
        this(source, new HostThread(trace.getHostId(), threadId));
    }

    /**
     * Constructor
     *
     * @param source
     *            the source
     * @param hostThread
     *            The Host thread to follow
     * @since 2.5
     */
    public TmfThreadSelectedSignal(Object source, HostThread hostThread) {
        super(source, 0, hostThread.getHost());
        fHostThread = hostThread;
    }

    /**
     * Get the thread ID
     *
     * @return the thead ID
     */
    public int getThreadId() {
        return fHostThread.getTid();
    }

    /**
     * Get the trace host id
     *
     * @return the trace host id
     */
    public String getTraceHost() {
        return fHostThread.getHost();
    }

    /**
     * Get the host thread that was selected
     *
     * @return The HostThread object
     * @since 3.1
     */
    public HostThread getHostThread() {
        return fHostThread;
    }

}
