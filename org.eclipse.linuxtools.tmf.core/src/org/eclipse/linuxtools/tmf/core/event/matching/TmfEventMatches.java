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

package org.eclipse.linuxtools.tmf.core.event.matching;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Class that does something with a match.
 *
 * This default implementation of the class just adds it to a list of matches
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class TmfEventMatches implements IMatchProcessingUnit {

    /**
     * The list of matches found
     */
    private final List<TmfEventDependency> fMatches;

    /**
     * Constructor
     */
    public TmfEventMatches() {
        fMatches = new ArrayList<>();
    }

    /**
     * IMatchProcessingUnit overrides
     */

    @Override
    public void init(ITmfTrace[] fTraces) {

    }

    @Override
    public void addMatch(TmfEventDependency match) {
        fMatches.add(match);
    }

    @Override
    public void matchingEnded() {

    }

    @Override
    public int countMatches() {
        return fMatches.size();
    }

    /**
     * Returns the match at the specified index
     *
     * @param index
     *            The index of the match to get
     * @return The match at index or null or not present
     */
    public TmfEventDependency getMatch(int index) {
        return fMatches.get(index);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [ Number of matches found: " + fMatches.size() + " ]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
