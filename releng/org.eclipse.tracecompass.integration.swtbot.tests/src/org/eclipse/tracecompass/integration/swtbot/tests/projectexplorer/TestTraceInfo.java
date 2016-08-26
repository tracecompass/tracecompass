/******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.integration.swtbot.tests.projectexplorer;

/**
 * A helper class to store information about a test trace.
 */
public class TestTraceInfo {
    private final String fTraceName;
    private final String fTracePath;
    private final String fTraceType;
    private final long fNbEvents;
    private final String fFirstEventTimestamp;

    /**
     *
     * @param traceName
     *            the name of the trace
     * @param traceType
     *            the trace type (Category with name format)
     * @param nbEvents
     *            the number of events in the trace
     * @param firstEventTimestamp
     *            he first event timestamp in string form. See
     *            {@link #getFirstEventTimestamp()}
     */
    public TestTraceInfo(String traceName, String traceType, long nbEvents, String firstEventTimestamp) {
        this(traceName, traceName, traceType, nbEvents, firstEventTimestamp);
    }

    /**
     *
     * @param traceName
     *            the name of the trace
     * @param tracePath
     *            the path of the trace. Whether or not this is absolute or
     *            relative is up to the client.
     * @param traceType
     *            the trace type (Category with name format)
     * @param nbEvents
     *            the number of events in the trace
     * @param firstEventTimestamp
     *            he first event timestamp in string form. See
     *            {@link #getFirstEventTimestamp()}
     */
    public TestTraceInfo(String traceName, String tracePath, String traceType, long nbEvents, String firstEventTimestamp) {
        fTraceName = traceName;
        fTracePath = tracePath;
        fTraceType = traceType;
        fNbEvents = nbEvents;
        fFirstEventTimestamp = firstEventTimestamp;
    }

    /**
     * @return the name of the trace
     */
    public String getTraceName() {
        return fTraceName;
    }

    /**
     * @return the path of the trace. Whether or not this is absolute or relative is up to the client.
     */
    public String getTracePath() {
        return fTracePath;
    }

    /**
     * @return the trace type (Category with name format)
     */
    public String getTraceType() {
        return fTraceType;
    }

    /**
     * @return the number of events in the trace
     */
    public long getNbEvents() {
        return fNbEvents;
    }

    /**
     * The first event timestamp in string form. Tests use this to see if the
     * cell contains this text (String.contains()). Since there can be timezone
     * issues with hours and days, this value should only specify minutes and
     * more precise digits. For example: 04:32.650 993 664
     *
     * @return the first event timestamp in string form
     */
    public String getFirstEventTimestamp() {
        return fFirstEventTimestamp;
    }
}