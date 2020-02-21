/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.tests.backend;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.HistoryTreeBackend;
import org.eclipse.tracecompass.statesystem.core.backend.IStateHistoryBackend;
import org.junit.After;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test the {@link HistoryTreeBackend} class.
 *
 * @author Patrick Tasse
 */
@RunWith(Parameterized.class)
public class HistoryTreeBackendTest extends StateHistoryBackendTestBase {

    /** State system ID */
    protected static final String SSID = "test";
    /** Provider version */
    protected static final int PROVIDER_VERSION = 0;

    /** Default maximum number of children nodes */
    protected static final int MAX_CHILDREN = 2;
    /** Default block size */
    protected static final int BLOCK_SIZE = 4096;

    /** ReOpen test parameter */
    protected final boolean fReOpen;

    /** Set of created history tree files */
    protected Set<File> fHistoryTreeFiles = new HashSet<>();
    /** Map of backends to history tree file */
    protected Map<IStateHistoryBackend, File> fBackendMap = new HashMap<>();
    /** Maximum number of children nodes */
    protected int fMaxChildren = MAX_CHILDREN;
    /** Block size */
    protected int fBlockSize = BLOCK_SIZE;

    /**
     * @return the test parameters
     */
    @Parameters(name = "ReOpen={0}")
    public static Collection<Boolean> parameters() {
        return Arrays.asList(Boolean.FALSE, Boolean.TRUE);
    }

    /**
     * Constructor
     *
     * @param reOpen
     *            True if the backend should be disposed and re-opened as a new
     *            backend from the file, or false to use the backend as-is
     */
    public HistoryTreeBackendTest(Boolean reOpen) {
        fReOpen = reOpen;
    }

    /**
     * Test cleanup
     */
    @After
    public void teardown() {
        for (IStateHistoryBackend backend : fBackendMap.keySet()) {
            backend.dispose();
        }
        for (File historyTreeFile : fHistoryTreeFiles) {
            historyTreeFile.delete();
        }
    }

    @Override
    protected IStateHistoryBackend getBackendForBuilding(long startTime) throws IOException {
        File historyTreeFile = checkNotNull(File.createTempFile("HistoryTreeBackendTest", ".ht"));
        fHistoryTreeFiles.add(historyTreeFile);
        HistoryTreeBackend backend = new HistoryTreeBackend(SSID, historyTreeFile, PROVIDER_VERSION, startTime, fBlockSize, fMaxChildren);
        fBackendMap.put(backend, historyTreeFile);
        return backend;
    }

    @Override
    protected IStateHistoryBackend getBackendForQuerying(IStateHistoryBackend backend) throws IOException {
        if (!fReOpen) {
            return backend;
        }

        File historyTreeFile = fBackendMap.remove(backend);

        if (historyTreeFile == null) {
            throw new IllegalStateException();
        }

        backend.dispose();
        HistoryTreeBackend reOpenedBackend = new HistoryTreeBackend(SSID, historyTreeFile, PROVIDER_VERSION);
        fBackendMap.put(reOpenedBackend, historyTreeFile);
        return reOpenedBackend;
    }
}
