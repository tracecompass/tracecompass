/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.trace;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.eclipse.tracecompass.tmf.core.trace.TraceValidationStatus;

/**
 * Trace validation status that contains additional information from a CTF trace
 * for further validation.
 *
 * @since 1.0
 */
public class CtfTraceValidationStatus extends TraceValidationStatus {

    private final Map<String, String> fEnvironment;
    private final Collection<String> fEventNames;

    /**
     * Constructor
     *
     * @param confidence
     *            the confidence level, 0 is lowest
     * @param pluginId
     *            the unique identifier of the relevant plug-in
     * @param environment
     *            the CTF trace environment variables
     */
    public CtfTraceValidationStatus(int confidence, String pluginId, Map<String, String> environment) {
        this(confidence, pluginId, environment, Collections.emptyList());
    }

    /**
     * Constructor
     *
     * @param confidence
     *            the confidence level, 0 is lowest
     * @param pluginId
     *            the unique identifier of the relevant plug-in
     * @param environment
     *            the CTF trace environment variables
     * @param eventNames
     *            The CTF event names
     * @since 4.1
     */
    public CtfTraceValidationStatus(int confidence, String pluginId, Map<String, String> environment, Collection<String> eventNames) {
        super(confidence, pluginId);
        fEnvironment = environment;
        fEventNames = eventNames;
    }

    /**
     * Get the CTF trace environment variables
     *
     * @return the CTF trace environment variables
     */
    public Map<String, String> getEnvironment() {
        return fEnvironment;
    }

    /**
     * Get the collection of event names
     *
     * @return the event names
     * @since 4.1
     */
    public Collection<String> getEventNames() {
        return fEventNames;
    }
}
