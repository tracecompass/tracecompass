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
 * A BTree visitor goes through the tree using a comparator for
 * optimal searches.
 *
 * @author Marc-Andre Laperle
 */
public interface IBTreeVisitor {

    /**
     * The current checkpoint being compared against an internally held key.
     *
     * @param checkpoint
     *            the current checkpoint
     * @return -1 if checkpoint < key, 0 if checkpoint == key, 1 if checkpoint >
     *         key
     */
    int compare(ITmfCheckpoint checkpoint);
}
