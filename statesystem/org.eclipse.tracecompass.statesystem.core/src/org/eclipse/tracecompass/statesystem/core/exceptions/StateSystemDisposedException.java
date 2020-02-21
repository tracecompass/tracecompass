/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.exceptions;

/**
 * Exception thrown by the state system if a query is done on it after it has
 * been disposed.
 *
 * @author Alexandre Montplaisir
 */
public class StateSystemDisposedException extends Exception {

    private static final long serialVersionUID = 7896041701818620084L;

    /**
     * Create a new simple StateSystemDisposedException.
     */
    public StateSystemDisposedException() {
        super();
    }

    /**
     * Create a new StateSystemDisposedException based on a previous one.
     *
     * @param e
     *            The previous exception
     */
    public StateSystemDisposedException(Throwable e) {
        super(e);
    }

}
