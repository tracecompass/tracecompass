/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.tests.backend;

import java.io.File;
import java.io.IOException;

import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.ThreadedHistoryTreeBackend;
import org.eclipse.tracecompass.statesystem.core.backend.IStateHistoryBackend;
import org.junit.Test;

/**
 * Test the {@link ThreadedHistoryTreeBackend} class.
 *
 * @author Patrick Tasse
 */
public class ThreadedHistoryTreeBackendTest extends HistoryTreeBackendTest {

    private static final int QUEUE_SIZE = 10;

    /**
     * Constructor
     *
     * @param reOpen
     *            True if the backend should be disposed and re-opened as a new
     *            backend from the file, or false to use the backend as-is
     */
    public ThreadedHistoryTreeBackendTest(Boolean reOpen) {
        super(reOpen);
    }

    @Override
    protected IStateHistoryBackend getBackendForBuilding(long startTime) throws IOException {
        File historyTreeFile = File.createTempFile("ThreadedHistoryTreeBackendTest", ".ht");
        fHistoryTreeFiles.add(historyTreeFile);
        ThreadedHistoryTreeBackend backend = new ThreadedHistoryTreeBackend(SSID, historyTreeFile, PROVIDER_VERSION, startTime, QUEUE_SIZE, fBlockSize, fMaxChildren);
        fBackendMap.put(backend, historyTreeFile);
        return backend;
    }

    @Override
    @Test
    public void testIntervalBeforeStart() {
        // Exception is thrown in a thread, so we can't catch it
    }

}
