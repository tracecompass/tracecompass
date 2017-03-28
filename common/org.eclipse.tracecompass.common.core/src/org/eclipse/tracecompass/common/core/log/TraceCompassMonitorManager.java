/*******************************************************************************
 * Copyright (c) 2022 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.common.core.log;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.common.core.log.TraceCompassMonitor;

/**
 * Trace Compass Monitor, shows the state of every scoped logger
 *
 * Use the {@link #update(String, long)} method to publish a new value
 *
 * @author Matthew Khouzam
 * @since 5.0
 */
public final class TraceCompassMonitorManager {

    private final Map<String, TraceCompassMonitor> fCounters = new LinkedHashMap<>();

    /**
     * Instance, internal, do not use
     */
    private static @Nullable TraceCompassMonitorManager sInstance = null;

    /**
     * Constructor
     */
    private TraceCompassMonitorManager() {
        // do nothing
    }

    /**
     * Update a value
     *
     * @param label
     *            the label to update
     * @param value
     *            the value to update for a given label
     */
    public synchronized void update(String label, long value) {
        fCounters.computeIfAbsent(label, TraceCompassMonitor::new).accept(value);
    }

    /**
     * Get the instance of the manager
     *
     * @return the manager
     */
    public static synchronized TraceCompassMonitorManager getInstance() {
        TraceCompassMonitorManager instance = sInstance;
        if (instance == null) {
            instance = new TraceCompassMonitorManager();
            sInstance = instance;
        }
        return instance;
    }
}
