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

import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Class that does something with a match.
 *
 * This default implementation of the class just counts the matches
 *
 * @author Geneviève Bastien
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
        // Do nothing
    }

    @Override
    public int countMatches() {
        return fMatchCount;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [ Number of matches found: " + fMatchCount + " ]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
