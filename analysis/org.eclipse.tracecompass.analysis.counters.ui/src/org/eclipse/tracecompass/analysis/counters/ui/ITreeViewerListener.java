/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.counters.ui;

import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeViewerEntry;

/**
 * Listens to changes in the accompanying tree viewer.
 *
 * This interface can be used by any chart which needs to modify its displayed
 * data depending on the selected entries in the tree.
 *
 * @author Mikael Ferland
 */
public interface ITreeViewerListener {

    /**
     * Handler for events where the tree viewer's selection has changed.
     *
     * @param entries
     *            Entries which have been checked in the tree
     */
    public void handleCheckStateChangedEvent(Iterable<ITmfTreeViewerEntry> entries);

}
