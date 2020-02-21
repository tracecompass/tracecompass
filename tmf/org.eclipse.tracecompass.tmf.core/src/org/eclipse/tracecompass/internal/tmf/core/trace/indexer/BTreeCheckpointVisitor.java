/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.trace.indexer;

import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;

/**
 * A visitor that searches for a specific checkpoint
 *
 * @author Marc-Andre Laperle
 */
public class BTreeCheckpointVisitor implements IBTreeVisitor {

    private long rank = -1;
    private ITmfCheckpoint found;
    private ITmfCheckpoint search;
    private boolean exactFound = false;

    /**
     * Constructs the checkpoint visitor
     *
     * @param search
     *            the checkpoint to search for
     */
    public BTreeCheckpointVisitor(ITmfCheckpoint search) {
        this.search = search;
    }

    @Override
    public int compare(ITmfCheckpoint currentCheckpoint) {
        int compareTo = currentCheckpoint.compareTo(search);
        if (compareTo <= 0 && !exactFound) {
            rank = currentCheckpoint.getCheckpointRank();
            found = currentCheckpoint;
            if (compareTo == 0) {
                exactFound = true;
            }
        }
        return compareTo;
    }

    /**
     * Return the found checkpoint
     *
     * @return the found checkpoint
     */
    public ITmfCheckpoint getCheckpoint() {
        return found;
    }

    /**
     * Returns the checkpoint rank of the searched checkpoint, if it is
     *         contained in the index; otherwise, (-(insertion point) - 1).
     *
     * @return the checkpoint rank of the searched checkpoint, if it is
     *         contained in the index; otherwise, (-(insertion point) - 1).
     */
    public long getCheckpointRank() {
        if (!exactFound) {
            long insertionPoint = rank + 1;
            return -insertionPoint - 1;
        }

        return rank;
    }
}