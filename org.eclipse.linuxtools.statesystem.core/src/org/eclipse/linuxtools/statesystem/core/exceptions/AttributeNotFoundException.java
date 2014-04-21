/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.linuxtools.statesystem.core.exceptions;

/**
 * This exception gets thrown when the user tries to access an attribute which
 * doesn't exist in the system, of if the quark is simply invalid (ie, < 0).
 *
 * @author Alexandre Montplaisir
 * @since 3.0
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

}
