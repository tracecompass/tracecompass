/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Patrick Tasse - Add message to exceptions
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.exceptions;

/**
 * This exception gets thrown when the user tries to access an attribute which
 * doesn't exist in the system, of if the quark is simply invalid (ie, < 0).
 *
 * @author Alexandre Montplaisir
 */
public class AttributeNotFoundException extends Exception {

    private static final long serialVersionUID = 7964275803369706145L;

    /**
     * Default constructor
     */
    public AttributeNotFoundException() {
        super();
    }

    /**
     * Constructor with a message
     *
     * @param message
     *            Message to attach to this exception
     */
    public AttributeNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor with both a message and a cause.
     *
     * @param message
     *            Message to attach to this exception
     * @param e
     *            Cause of this exception
     * @since 1.0
     */
    public AttributeNotFoundException(String message, Throwable e) {
        super(message, e);
    }
}
