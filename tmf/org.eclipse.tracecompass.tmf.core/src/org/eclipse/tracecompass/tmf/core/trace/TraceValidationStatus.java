/*******************************************************************************
 * Copyright (c) 2014 Ericsson
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

package org.eclipse.tracecompass.tmf.core.trace;

import org.eclipse.core.runtime.Status;

/**
 * A class representing the validation status of a trace against a particular
 * trace type.
 */
public class TraceValidationStatus extends Status {

    private int fConfidence;

    /**
     * Construct a successful validation status with a confidence level
     *
     * @param confidence
     *            the confidence level, 0 is lowest
     * @param pluginId
     *            the unique identifier of the relevant plug-in
     */
    public TraceValidationStatus(int confidence, String pluginId) {
        this(confidence, OK, pluginId, OK_STATUS.getMessage(), null);
    }

    /**
     * Full constructor for construct a validation status with a confidence
     * level, severity and exception
     *
     * @param confidence
     *            the confidence level, 0 is lowest
     * @param severity
     *            the severity; one of <code>OK</code>, <code>ERROR</code>,
     *            <code>INFO</code>, <code>WARNING</code>, or
     *            <code>CANCEL</code>
     * @param pluginId
     *            the unique identifier of the relevant plug-in
     * @param message
     *            a human-readable message, localized to the current locale
     * @param exception
     *            a low-level exception, or <code>null</code> if not applicable
     * @since 1.0
     */
    public TraceValidationStatus(int confidence, int severity, String pluginId, String message, Throwable exception) {
        super(severity, pluginId, message, exception);
        if (confidence < 0) {
            throw new IllegalArgumentException();
        }
        fConfidence = confidence;
    }

    /**
     * Gets the confidence level
     *
     * @return the confidence level, 0 is lowest
     */
    public int getConfidence() {
        return fConfidence;
    }
}
