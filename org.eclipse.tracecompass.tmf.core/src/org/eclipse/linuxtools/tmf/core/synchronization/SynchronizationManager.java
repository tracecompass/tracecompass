/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.synchronization;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.tmf.core.component.TmfComponent;
import org.eclipse.linuxtools.tmf.core.event.matching.ITmfEventMatching;
import org.eclipse.linuxtools.tmf.core.event.matching.TmfNetworkEventMatching;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * This abstract manager class handles loading trace synchronization data or
 * otherwise their calculation.
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public abstract class SynchronizationManager extends TmfComponent {

    /**
     * Function called to synchronize traces using the fully incremental
     * synchronization algorithm
     *
     * @param syncFile
     *            The target name of the synchronization file. If it exists, it
     *            will be opened, otherwise it will be created and data from
     *            this synchro run will be saved there
     * @param traces
     *            The list of traces to synchronize
     * @param doSync
     *            Whether to actually synchronize or just try opening a sync
     *            file
     * @return The synchronization object
     */
    public static SynchronizationAlgorithm synchronizeTraces(final File syncFile, final Collection<ITmfTrace> traces, boolean doSync) {

        SynchronizationAlgorithm syncAlgo;
        if (doSync) {
            syncAlgo = synchronize(syncFile, traces, SynchronizationAlgorithmFactory.getDefaultAlgorithm());
        } else {
            syncAlgo = openExisting(syncFile);
            if (syncAlgo == null) {
                syncAlgo = SynchronizationAlgorithmFactory.getDefaultAlgorithm();
            }
        }
        return syncAlgo;
    }

    /**
     * Function called to synchronize traces with a specific synchronization
     * algorithm. If a synchronization already exists, but is not the requested
     * algorithm, the synchronization is done again using the new algorithm
     *
     * @param syncFile
     *            The target name of the synchronization file. If it exists, it
     *            will be opened, otherwise it will be created and data from
     *            this synchro run will be saved there
     * @param traces
     *            The list of traces to synchronize
     * @param algo
     *            A synchronization algorithm object to determine the algorithm
     *            used to synchronization.
     * @param doSync
     *            Whether to actually synchronize or just try opening a sync
     *            file
     * @return The synchronization object
     */
    public static SynchronizationAlgorithm synchronizeTraces(final File syncFile, final Collection<ITmfTrace> traces, SynchronizationAlgorithm algo, boolean doSync) {

        SynchronizationAlgorithm syncAlgo;
        if (doSync) {
            syncAlgo = synchronize(syncFile, traces, algo);
        } else {
            syncAlgo = openExisting(syncFile);
            if (syncAlgo == null || (syncAlgo.getClass() != algo.getClass())) {
                if (algo != null) {
                    syncAlgo = algo;
                } else {
                    syncAlgo = SynchronizationAlgorithmFactory.getDefaultAlgorithm();
                }
            }
        }

        return syncAlgo;
    }

    private static SynchronizationAlgorithm openExisting(final File syncFile) {
        if ((syncFile != null) && syncFile.exists()) {
            /* Load an existing history */
            try {
                SynchronizationBackend syncBackend = new SynchronizationBackend(syncFile);
                SynchronizationAlgorithm algo = syncBackend.openExistingSync();
                return algo;
            } catch (IOException e) {
                /*
                 * There was an error opening the existing file. Perhaps it was
                 * corrupted, perhaps it's an old version? We'll just
                 * fall-through and try to build a new one from scratch instead.
                 */
                Activator.logInfo("Problem opening existing trace synchronization file", e); //$NON-NLS-1$
            }
        }
        return null;
    }

    private static SynchronizationAlgorithm synchronize(final File syncFile, final Collection<ITmfTrace> traces, SynchronizationAlgorithm syncAlgo) {
        ITmfEventMatching matching = new TmfNetworkEventMatching(traces, syncAlgo);
        matching.matchEvents();

        SynchronizationBackend syncBackend;
        try {
            syncBackend = new SynchronizationBackend(syncFile, false);
            syncBackend.saveSync(syncAlgo);
        } catch (IOException e) {
            Activator.logError("Error while saving trace synchronization file", e); //$NON-NLS-1$
        }
        return syncAlgo;
    }

}
