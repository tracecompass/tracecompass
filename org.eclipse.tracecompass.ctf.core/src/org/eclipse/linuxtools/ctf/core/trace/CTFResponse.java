/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.ctf.core.trace;

/**
 * A response to a request
 *
 * @author Matthew Khouzam
 * @since 3.0
 *
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
