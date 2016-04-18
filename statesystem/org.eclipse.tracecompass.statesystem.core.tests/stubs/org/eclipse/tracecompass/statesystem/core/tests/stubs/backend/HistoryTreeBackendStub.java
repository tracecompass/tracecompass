/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.tests.stubs.backend;

import java.io.File;
import java.io.IOException;

import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.HTConfig;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.HistoryTree;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.HistoryTreeBackend;

/**
 * Stub class for the {@link HistoryTreeBackend}. It creates a
 * {@link HistoryTreeStub} to grant access to some protected methods.
 *
 * @author Geneviève Bastien
 */
public class HistoryTreeBackendStub extends HistoryTreeBackend {

    /**
     * Constructor for new history files. Use this when creating a new history
     * from scratch.
     *
     * @param ssid
     *            The state system's ID
     * @param newStateFile
     *            The filename/location where to store the state history (Should
     *            end in .ht)
     * @param providerVersion
     *            Version of of the state provider. We will only try to reopen
     *            existing files if this version matches the one in the
     *            framework.
     * @param startTime
     *            The earliest time stamp that will be stored in the history
     * @param blockSize
     *            The size of the blocks in the history file. This should be a
     *            multiple of 4096.
     * @param maxChildren
     *            The maximum number of children each core node can have
     * @throws IOException
     *             Thrown if we can't create the file for some reason
     */
    public HistoryTreeBackendStub(String ssid,
            File newStateFile,
            int providerVersion,
            long startTime,
            int blockSize,
            int maxChildren) throws IOException {
        super(ssid, newStateFile, providerVersion, startTime, blockSize, maxChildren);
    }

    /**
     * Existing history constructor. Use this to open an existing state-file.
     *
     * @param ssid
     *            The state system's id
     * @param existingStateFile
     *            Filename/location of the history we want to load
     * @param providerVersion
     *            Expected version of of the state provider plugin.
     * @throws IOException
     *             If we can't read the file, if it doesn't exist, is not
     *             recognized, or if the version of the file does not match the
     *             expected providerVersion.
     */
    public HistoryTreeBackendStub(String ssid, File existingStateFile, int providerVersion)
            throws IOException {
        super(ssid, existingStateFile, providerVersion);
    }

    @Override
    protected HistoryTree initializeSHT(HTConfig conf) throws IOException {
        return new HistoryTreeStub(conf);
    }

    @Override
    protected HistoryTree initializeSHT(File existingStateFile, int providerVersion) throws IOException {
        return new HistoryTreeStub(existingStateFile, providerVersion);
    }

    /**
     * Get the History Tree built by this backend.
     *
     * @return The history tree
     */
    public HistoryTreeStub getHistoryTree() {
        return (HistoryTreeStub) super.getSHT();
    }

}
