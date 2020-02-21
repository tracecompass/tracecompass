/*******************************************************************************
 * Copyright (c) 2012, 2017 Ericsson and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.datastore.core.exceptions;

/**
 * Generic exception for when the user specifies an history range. Usually
 * history range must be within the range of the trace or state history being
 * queried.
 *
 * For insertions, it's forbidden to insert new states "in the past" (before
 * where the cursor is), so this exception is also thrown in that case.
 *
 * @author Alexandre Montplaisir
 */
public class RangeException extends RuntimeException {

    private static final long serialVersionUID = -4067685227260254532L;

    /**
     * Default constructor
     */
    public RangeException() {
    }

    /**
     * Constructor with a message
     *
     * @param message
     *            Message to attach to this exception
     */
    public RangeException(String message) {
        super(message);
    }

    /**
     * Constructor with both a message and a cause.
     *
     * @param message
     *            Message to attach to this exception
     * @param e
     *            Cause of this exception
     */
    public RangeException(String message, Throwable e) {
        super(message, e);
    }
}
