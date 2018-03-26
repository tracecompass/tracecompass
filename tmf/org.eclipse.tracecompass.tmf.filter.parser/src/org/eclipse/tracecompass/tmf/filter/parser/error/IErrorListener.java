/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
