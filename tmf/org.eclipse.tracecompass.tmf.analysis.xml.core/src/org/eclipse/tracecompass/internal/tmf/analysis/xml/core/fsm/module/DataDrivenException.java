/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * A class representing an exception during data-driven analysis execution
 *
 * @author Geneviève Bastien
 */
public class DataDrivenException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 7253243310720136437L;
    private final ITmfEvent fEvent;

    /**
     * Constructor
     *
     * @param string
     *            A string representing this error
     * @param event
     *            The event for which this error occurred.
     */
    public DataDrivenException(String string, ITmfEvent event) {
        super(string);
        fEvent = event;
    }

    /**
     * Get the event for which the exception occurred
     *
     * @return The event
     */
    public ITmfEvent getEvent() {
        return fEvent;
    }

}
