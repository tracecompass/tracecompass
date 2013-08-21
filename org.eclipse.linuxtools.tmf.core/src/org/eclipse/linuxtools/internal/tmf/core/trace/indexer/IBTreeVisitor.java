/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.trace.indexer;

import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;

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
