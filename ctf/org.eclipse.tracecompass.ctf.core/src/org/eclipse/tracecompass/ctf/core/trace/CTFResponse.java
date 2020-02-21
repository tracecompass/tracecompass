/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.ctf.core.trace;

/**
 * A response to a request
 *
 * @author Matthew Khouzam
 */
public enum CTFResponse {
    /**
     * The operation was successful
     */
    OK,
    /**
     * The operation cannot be yet completed
     */
    WAIT,
    /**
     * The operation was finished
     */
    FINISH,
    /**
     * The operation failed
     */
    ERROR
}
