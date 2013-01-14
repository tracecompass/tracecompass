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

package org.eclipse.linuxtools.tmf.core.statesystem;

import java.io.File;
import java.io.IOException;

import org.eclipse.linuxtools.internal.tmf.core.statesystem.HistoryBuilder;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.IStateHistoryBackend;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.InMemoryBackend;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.NullBackend;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.historytree.HistoryTreeBackend;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.historytree.ThreadedHistoryTreeBackend;
import org.eclipse.linuxtools.tmf.core.component.TmfComponent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;

/**
 * This abstract manager class handles loading or creating state history files
 * for use in TMF's generic state system.
 *
 * @version 1.0
 * @author Alexandre Montplaisir
 */
public abstract class StateSystemManager extends TmfComponent {

    /** Size of the blocking queue to use when building a state history */
    private final static int QUEUE_SIZE = 10000;

    /**
     * Load the history file matching the target trace. If the file already
     * exists, it will be opened directly. If not, it will be created from
     * scratch. In the case the history has to be built, it's possible to block
     * the calling thread until construction is complete.
     *
     * @param htFile
     *            The target name of the history file we want to use. If it
     *            exists it will be opened. If it doesn't, a new file will be
     *            created with this name/path.
     * @param htInput
     *            The IStateChangeInput to use for building the history file. It
     *            may be required even if we are opening an already-existing
     *            history (ie, for partial histories).
     * @param buildManually
     *            If false, the construction will wait for a signal before
     *            starting. If true, it will build everything right now and
     *            block the caller. It has no effect if the file already exists.
     * @return A IStateSystemQuerier handler to the state system, with which you
     *         can then run queries on the history.
     * @throws TmfTraceException
     *             If there was a problem reading or writing one of the files.
     *             See the contents of this exception for more info.
     * @since 2.0
     */
    public static ITmfStateSystem loadStateHistory(File htFile,
            IStateChangeInput htInput, boolean buildManually)
            throws TmfTraceException {
        ITmfStateSystem ss;
        IStateHistoryBackend htBackend;

        /* If the target file already exists, do not rebuild it uselessly */
        // TODO for now we assume it's complete. Might be a good idea to check
        // at least if its range matches the trace's range.
        if (htFile.exists()) {
            /* Load an existing history */
            try {
                htBackend = new HistoryTreeBackend(htFile);
                ss = HistoryBuilder.openExistingHistory(htBackend);
                return ss;
            } catch (IOException e) {
                /*
                 * There was an error opening the existing file. Perhaps it was
                 * corrupted, perhaps it's an old version? We'll just
                 * fall-through and try to build a new one from scratch instead.
                 */
            }
        }

        /* Create a new state history from scratch */
        HistoryBuilder builder;

        if (htInput == null) {
            return null;
        }
        try {
            htBackend = new ThreadedHistoryTreeBackend(htFile,
                    htInput.getStartTime(), QUEUE_SIZE);
            builder = new HistoryBuilder(htInput, htBackend, buildManually);
        } catch (IOException e) {
            /*
             * If it fails here however, it means there was a problem writing to
             * the disk, so throw a real exception this time.
             */
            throw new TmfTraceException(e.toString(), e);
        }
        return builder.getStateSystemQuerier();
    }

    /**
     * Create a new state system using a null history back-end. This means that
     * no history intervals will be saved anywhere, and as such only
     * {@link ITmfStateSystem#queryOngoingState} will be available.
     *
     * This has to be built "manually" (which means you should call
     * input.processEvent() to update the ongoing state of the state system).
     *
     * @param input
     *            The input plugin to build the history
     * @return Reference to the history-less state system that got built
     * @since 2.0
     */
    public static ITmfStateSystem newNullHistory(IStateChangeInput input) {
        IStateHistoryBackend backend = new NullBackend();
        HistoryBuilder builder = new HistoryBuilder(input, backend, true);
        ITmfStateSystem ss = builder.getStateSystemQuerier();
        return ss;
    }

    /**
     * Create a new state system using in-memory interval storage. This should
     * only be done for very small state system, and will be naturally limited
     * to 2^31 intervals.
     *
     * This will block the caller while the construction is ongoing.
     *
     * @param input
     *            The state change input to use
     * @param buildManually
     *            Set to true to block the caller and build without using TMF
     *            signals (for test programs most of the time). Use false if you
     *            are using the TMF facilities (experiments, etc.)
     * @return Reference to the state system that just got built
     * @since 2.0
     */
    public static ITmfStateSystem newInMemHistory(IStateChangeInput input,
            boolean buildManually) {
        IStateHistoryBackend backend = new InMemoryBackend(input.getStartTime());
        HistoryBuilder builder = new HistoryBuilder(input, backend, buildManually);
        return builder.getStateSystemQuerier();
    }
}
