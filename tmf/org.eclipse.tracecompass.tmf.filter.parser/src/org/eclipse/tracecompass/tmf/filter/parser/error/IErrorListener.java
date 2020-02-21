/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
package org.eclipse.tracecompass.tmf.filter.parser.error;

/**
 * Error handler for the filter lexer and parser
 * 
 * @author Jean-Christian Kouame
 *
 */
public interface IErrorListener {

    /**
     * Error handler call whenever an exception happen in the lexer or the parser of
     * the filter
     * 
     * @param e
     *            The caught exception
     */
    void error(Exception e);
}
