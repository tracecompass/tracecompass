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

package org.eclipse.tracecompass.analysis.os.linux.core.model;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * This class represents a thread from a specific host. Many machines in an
 * experiment can have the same thread IDs. This class differentiates the
 * threads by adding the host ID it belongs to.
 *
 * @author Geneviève Bastien
 * @since 1.0
 */
public class HostThread {

    /**
     * The key for the trace context's currently selected host thread. The
     * corresponding data value should be of {@link HostThread} class
     * @since 3.2
     */
    public static final String SELECTED_HOST_THREAD_KEY = "model.selectedThread"; //$NON-NLS-1$

    private static final HashFunction HF = NonNullUtils.checkNotNull(Hashing.goodFastHash(32));

    private final String fHost;
    private final Integer fTid;

    /**
     * Constructor
     *
     * @param host
     *            The host this thread belongs to
     * @param tid
     *            The thread ID of this thread
     */
    public HostThread(String host, Integer tid) {
        fHost = host;
        fTid = tid;
    }

    @Override
    public int hashCode() {
        return HF.newHasher()
                .putUnencodedChars(fHost)
                .putInt(fTid).hash().asInt();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o instanceof HostThread) {
            HostThread hostTid = (HostThread) o;
            if (fTid.equals(hostTid.fTid) &&
                    fHost.equals(hostTid.fHost)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "HostTid: [" + fHost + ',' + fTid + ']'; //$NON-NLS-1$
    }

    /**
     * Get the thread ID of this thread
     *
     * @return The thread ID of this thread
     */
    public Integer getTid() {
        return fTid;
    }

    /**
     * Get the host ID of the machine this thread belongs to
     *
     * @return The host ID this thread belongs to
     */
    public String getHost() {
        return fHost;
    }

}