/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.tests.stubs.trace;

import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfEventParser;

/**
 * Another stub trace class for unit tests who need more than one stub class.
 *
 * @author Geneviève Bastien
 */
public class TmfTraceStub2 extends TmfTraceStub {

    /**
     * Default constructor
     */
    public TmfTraceStub2() {
        super();
    }

    /**
     * Constructor to specify the parser and indexer. The streaming interval
     * will be 0.
     *
     * @param path
     *            The path to the trace file
     * @param cacheSize
     *            The cache size
     * @param waitForCompletion
     *            Do we block the caller until the trace is indexed, or not.
     * @param parser
     *            The trace parser. If left 'null', it will use a
     *            {@link TmfEventParserStub}.
     * @throws TmfTraceException
     *             If an error occurred opening the trace
     */
    public TmfTraceStub2(final String path,
            final int cacheSize,
            final boolean waitForCompletion,
            final ITmfEventParser parser) throws TmfTraceException {
        super(path, cacheSize, waitForCompletion, parser);
    }

}
