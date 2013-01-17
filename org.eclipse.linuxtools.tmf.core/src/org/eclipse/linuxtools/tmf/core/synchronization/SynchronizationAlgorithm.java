/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.synchronization;

import java.io.Serializable;
import java.util.Map;

import org.eclipse.linuxtools.tmf.core.event.matching.TmfEventDependency;
import org.eclipse.linuxtools.tmf.core.event.matching.TmfEventMatches;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Abstract class for synchronization algorithm
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public abstract class SynchronizationAlgorithm extends TmfEventMatches implements Serializable {

    private static final long serialVersionUID = -3083906749528872196L;

    /**
     * Quality of the result obtained by the synchronization algorithm
     */
    public enum SyncQuality {
        /**
         * Algorithm returned a result satisfying all hypothesis for the
         * algorithm
         */
        ACCURATE,
        /**
         * Best effort of the algorithm
         */
        APPROXIMATE,
        /**
         * There is communication only in one direction
         */
        INCOMPLETE,
        /**
         * No communication between two traces
         */
        ABSENT,
        /**
         * Hypothesis of the algorithm are not satisfied for some reason
         */
        FAIL
    }

    @Override
    public void addMatch(TmfEventDependency match) {
        super.addMatch(match);
        processMatch(match);
    }

    /**
     * Function for synchronization algorithm to do something with the received
     * match
     *
     * @param match
     *            The match of events
     */
    protected abstract void processMatch(TmfEventDependency match);

    /**
     * Returns a map of staticstics relating to this algorithm. Those stats
     * could be used to be displayed in a view for example.
     *
     * @return A map of statistics for this algorithm
     */
    public abstract Map<String, Map<String, Object>> getStats();

    /**
     * Returns a timestamp transformation algorithm
     *
     * @param trace
     *            The trace to get the transform for
     * @return The timestamp transformation formula
     */
    public abstract ITmfTimestampTransform getTimestampTransform(ITmfTrace trace);

    /**
     * Returns a timestamp transformation algorithm
     *
     * @param name
     *            The name of the trace to get the transform for
     * @return The timestamp transformation formula
     */
    public abstract ITmfTimestampTransform getTimestampTransform(String name);

    /**
     * Gets the quality of the synchronization between two given traces
     *
     * @param trace1
     *            First trace
     * @param trace2
     *            Second trace
     * @return The synchronization quality
     */
    public abstract SyncQuality getSynchronizationQuality(ITmfTrace trace1, ITmfTrace trace2);

    /**
     * Returns whether a given trace has a synchronization formula that is not
     * identity. This function returns true if the synchronization algorithm has
     * failed for some reason
     *
     * @param name
     *            The name of the trace
     * @return true if trace has formula
     */
    public abstract boolean isTraceSynced(String name);

    /**
     * Rename a trace involved in this algorithm. This function is necessary
     * because after synchronization, the trace whose timestamp changes is
     * copied with a new name and the synchronization needs to keep track of the
     * new name.
     *
     * @param oldname
     *            Original name of the trace
     * @param newname
     *            New name of the trace
     */
    public abstract void renameTrace(String oldname, String newname);

}
