/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.analysis.callsite;

import java.util.Iterator;
import java.util.List;

import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfCallsite;

/**
 * Callsite source, will give callsites for a given device at a given time
 *
 * @author Matthew Khouzam
 * @since 5.1
 */
public interface ITmfCallsiteResolver {
    /**
     * Get the callsites for a given category and time
     *
     * @param hostId
     *            a host ID, e.g. PCI1 or PCI2 in the case of multi-gpu or the
     *            trace UUID in the case of CPU
     * @param deviceType
     *            the device type (cpu, gpu, dsp, asic...) to query
     * @param deviceId
     *            an id for the device type
     * @param time
     *            the time to query at, in nanoseconds
     * @return a list of callsites. May be empty
     */
    List<ITmfCallsite> getCallsites(String hostId, String deviceType, String deviceId, long time);

    /**
     * Get a callsite iterator
     *
     * @param hostId
     *            hostId to iterate over
     * @param deviceType
     *            the device type (cpu, gpu, dsp, asic...) to query
     * @param deviceId
     *            an id for the device type
     * @param initialTime
     *            initial time
     * @return the iterator that will traverse the callsites for the given
     *         parameters
     */
    Iterator<TimeCallsite> iterator(String hostId, String deviceType, String deviceId, long initialTime);
}
