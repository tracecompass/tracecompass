/**********************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 **********************************************************************/

package org.eclipse.linuxtools.tmf.core.project.model;

/**
 * Tmf trace import exception
 *
 * @author Matthew Khouzam
 * @since 3.0
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
