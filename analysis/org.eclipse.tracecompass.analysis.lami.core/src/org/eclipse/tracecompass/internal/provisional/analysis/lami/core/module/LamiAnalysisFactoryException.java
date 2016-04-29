/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Philippe Proulx
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module;

/**
 * Class-specific exception, for when things go wrong with the
 * {@link LamiAnalysisFactoryFromConfigFile}.
 */
public class LamiAnalysisFactoryException extends Exception {

    private static final long serialVersionUID = 1349804105078874111L;

    /**
     * Default constructor
     */
    public LamiAnalysisFactoryException() {
        super();
    }

    /**
     * Constructor specifying a message
     *
     * @param message
     *            The exception message
     */
    public LamiAnalysisFactoryException(String message) {
        super(message);
    }

    /**
     * Constructor specifying both a cause and a message
     *
     * @param message
     *            The exception message
     * @param cause
     *            The exception that caused this one
     */
    public LamiAnalysisFactoryException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor specifying a cause
     *
     * @param cause
     *            The exception that caused this one
     */
    public LamiAnalysisFactoryException(Throwable cause) {
        super(cause);
    }

}