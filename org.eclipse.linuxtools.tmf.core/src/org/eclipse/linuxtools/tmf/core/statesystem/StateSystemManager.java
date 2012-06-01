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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.HistoryBuilder;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.IStateHistoryBackend;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.historytree.HistoryTreeBackend;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.historytree.ThreadedHistoryTreeBackend;
import org.eclipse.linuxtools.tmf.core.component.TmfComponent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * This abstract manager class handles loading or creating state history files
 * for use in TMF's generic state system.
 * 
 * @author alexmont
 * 
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
     * @param waitForCompletion
     *            Should we block the calling thread until the construction is
     *            complete? It has no effect if the file already exists.
     * @return A IStateSystemQuerier handler to the state system, with which you
     *         can then run queries on the history.
     * @throws TmfTraceException
     */
    public static IStateSystemQuerier loadStateHistory(File htFile,
            IStateChangeInput htInput, boolean waitForCompletion)
            throws TmfTraceException {
        IStateSystemQuerier ss;
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
            builder = new HistoryBuilder(htInput, htBackend);
        } catch (IOException e) {
            /*
             * If it fails here however, it means there was a problem writing to
             * the disk, so throw a real exception this time.
             */
            throw new TmfTraceException(e.toString(), e);
        }
        startBuilderJob(builder, htInput.getTrace(), waitForCompletion);
        return builder.getStateSystemQuerier();
    }

    private static void startBuilderJob(final HistoryBuilder builder,
            ITmfTrace<?> trace, boolean waitForCompletion) {

        final Job job = new Job("Building state history for " +
                trace.getName() + "...") { //$NON-NLS-1$
            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                builder.run();
                monitor.done();
                // send signal here?
                return Status.OK_STATUS;
            }
        };

        job.schedule();
        if (waitForCompletion) {
            try {
                job.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
