/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.exceptions;

/**
 * Generic exception for an error or issue occurs in analysis setup and
 * execution.
 *
 * For instance, to perform an analysis, a trace must be of the right type and
 * have some characteristics. If trying to do an analysis on a trace that does
 * not match, this exception is thrown.
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class TmfAnalysisException extends Exception {

    private static final long serialVersionUID = -4567750551324478401L;

    /**
     * Default constructor
     */
    public TmfAnalysisException() {
        super();
    }

    /**
     * Constructor with a message
     *
     * @param message
     *            Message to attach to this exception
     */
    public TmfAnalysisException(String message) {
        super(message);
    }

}
