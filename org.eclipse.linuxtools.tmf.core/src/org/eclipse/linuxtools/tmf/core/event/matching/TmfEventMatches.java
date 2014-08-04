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

import java.util.Collection;

import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Class that does something with a match.
 *
 * This default implementation of the class just counts the matches
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class TmfEventMatches implements IMatchProcessingUnit {

    private int fMatchCount;

    /**
     * Constructor
     */
    public TmfEventMatches() {
        fMatchCount = 0;
    }

    /**
     * IMatchProcessingUnit overrides
     */

    @Override
    public void init(Collection<ITmfTrace> fTraces) {
        fMatchCount = 0;
    }

    @Override
    public void addMatch(TmfEventDependency match) {
        fMatchCount++;
    }

    @Override
    public void matchingEnded() {

    }

    @Override
    public int countMatches() {
        return fMatchCount;
    }

    /**
     * Returns the match at the specified index
     *
     * @param index
     *            The index of the match to get
     * @return The match at index or null or not present
     * @deprecated Matches are not kept anymore, they use up memory for no real reason
     */
    @Deprecated
    public TmfEventDependency getMatch(int index) {
        return null;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [ Number of matches found: " + fMatchCount + " ]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
