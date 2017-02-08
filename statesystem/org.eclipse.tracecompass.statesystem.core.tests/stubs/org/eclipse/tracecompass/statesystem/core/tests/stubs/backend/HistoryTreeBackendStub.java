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
import java.io.PrintWriter;

import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.HTConfig;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.HistoryTreeBackend;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.IHistoryTree;

/**
 * Stub class for the {@link HistoryTreeBackend}. It creates a
 * {@link HistoryTreeClassicStub} to grant access to some protected methods.
 *
 * @author Geneviève Bastien
 */
public class HistoryTreeBackendStub extends HistoryTreeBackend {

    private static HistoryTreeType HT_TYPE = HistoryTreeType.CLASSIC;

    /**
     * Sets the type of tree to build. Since the history tree is initialized in
     * the parent's constructor, this stub class needs to know the type of tree
     * to build.
     *
     * @param htType
     *            The type of history tree to build for this backend
     */
    public static void setTreeType(HistoryTreeType htType) {
        HT_TYPE = htType;
    }

    /**
     * Enumeration of all history tree types implemented. This will be used to
     * create the right type of history tree
     */
    public enum HistoryTreeType {
        /**
         * The classic history tree
         */
        CLASSIC
    }

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
    protected IHistoryTree initializeSHT(HTConfig conf) throws IOException {
        switch (HT_TYPE) {
        case CLASSIC:
            return new HistoryTreeClassicStub(conf);
        default:
            return new HistoryTreeClassicStub(conf);
        }
    }

    @Override
    protected IHistoryTree initializeSHT(File existingStateFile, int providerVersion) throws IOException {
        switch (HT_TYPE) {
        case CLASSIC:
            return new HistoryTreeClassicStub(existingStateFile, providerVersion);
        default:
            return new HistoryTreeClassicStub(existingStateFile, providerVersion);
        }
    }

    /**
     * Get the History Tree built by this backend.
     *
     * @return The history tree
     */
    public HistoryTreeClassicStub getHistoryTree() {
        return (HistoryTreeClassicStub) super.getSHT();
    }

    /**
     * Debug method to print the contents of the history backend.
     *
     * @param writer
     *            The PrintWriter where to write the output
     */
    public void debugPrint(PrintWriter writer) {
        /* By default don't print out all the intervals */
        debugPrint(writer, false, -1);
    }

    /**
     * The basic debugPrint method will print the tree structure, but not their
     * contents.
     *
     * This method here print the contents (the intervals) as well.
     *
     * @param writer
     *            The PrintWriter to which the debug info will be written
     * @param printIntervals
     *            Should we also print every contained interval individually?
     * @param ts
     *            The timestamp that nodes have to intersect for intervals to be
     *            printed. A negative value will print intervals for all nodes.
     *            The timestamp only applies if printIntervals is true.
     */
    public void debugPrint(PrintWriter writer, boolean printIntervals, long ts) {
        /* Only used for debugging, shouldn't be externalized */
        writer.println("------------------------------"); //$NON-NLS-1$
        writer.println("State History Tree:\n"); //$NON-NLS-1$
        writer.println(getSHT().toString());
        writer.println("Average node utilization: " //$NON-NLS-1$
                + getAverageNodeUsage());
        writer.println(""); //$NON-NLS-1$

        getHistoryTree().debugPrintFullTree(writer, printIntervals, ts);
    }
}
