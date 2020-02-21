/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event.matching;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * This class represent an action to be done when event matches are found. This
 * interface needs to be implemented by all classes that want to be warned when
 * new event matches are found. They need to register to an instance of
 * TmfEventMatches class in order to be informed of matches.
 *
 * @author Geneviève Bastien
 */
@NonNullByDefault
public interface IMatchProcessingUnit {

    /**
     * Once the traces are known, hook function to initialize some things
     *
     * @param fTraces the set of traces that will be synchronized
     */
    void init(Collection<ITmfTrace> fTraces);

    /**
     * Function called when a match is found
     *
     * @param match
     *            The event match
     */
    void addMatch(TmfEventDependency match);

    /**
     * Function called when a match is found
     *
     * @param eventKey
     *            The key that matched the events to get this match
     * @param match
     *            The event match
     * @since 3.3
     */
    default void addMatch(IEventMatchingKey eventKey, TmfEventDependency match) {
        addMatch(match);
    }

    /**
     * Function called after all matching has been done, to do any post-match
     * treatment
     */
    void matchingEnded();

    /**
     * Counts the matches
     *
     * @return the number of matches
     */
    int countMatches();

}
