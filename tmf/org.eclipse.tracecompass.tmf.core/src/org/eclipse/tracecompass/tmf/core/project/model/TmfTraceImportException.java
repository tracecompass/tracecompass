/**********************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.project.model;

/**
 * Tmf trace import exception
 *
 * @author Matthew Khouzam
 */
public class TmfTraceImportException extends Exception {

    private static final long serialVersionUID = -6902068313782751330L;

    /**
     * Constructs a new TmfTraceImportException with <code>null</code> as its
     * detail message. The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     */
    public TmfTraceImportException() {
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized, and may subsequently be initialized by a call to
     * {@link #initCause}.
     *
     * @param message
     *            the detail message. The detail message is saved for later
     *            retrieval by the {@link #getMessage()} method.
     */
    public TmfTraceImportException(String message) {
        super(message);
    }
}
