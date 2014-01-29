/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace;

import org.eclipse.core.runtime.Status;

/**
 * A class representing the validation status of a trace against a particular
 * trace type.
 *
 * @since 3.0
 */
public class TraceValidationStatus extends Status {

    private int fConfidence;

    /**
     * Construct a successful validation status with a confidence level
     *
     * @param confidence the confidence level, 0 is lowest
     * @param pluginId the unique identifier of the relevant plug-in
     */
    public TraceValidationStatus(int confidence, String pluginId) {
        super(OK, pluginId, OK_STATUS.getMessage());
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
