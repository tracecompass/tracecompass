/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.counters.ui;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeViewerEntry;

/**
 * Entry of a TMF tree viewer which is associated to a counter.
 *
 * @author Matthew Khouzam
 * @author Mikael Ferland
 */
public class CounterTreeViewerEntry extends TmfTreeViewerEntry {

    private final int fQuark;
    private final ITmfStateSystem fStateSystem;
    private final @NonNull String fFullPath;

    /**
     * Constructor
     *
     * @param quark
     *            ID of the entry
     * @param stateSystem
     *            State system which contains the counter
     * @param fullPath
     *            Slash-separated path of the entry in the state system
     */
    public CounterTreeViewerEntry(int quark, ITmfStateSystem stateSystem, @NonNull String fullPath) {
        super(stateSystem.getAttributeName(quark));
        fQuark = quark;
        fStateSystem = stateSystem;
        fFullPath = fullPath;
    }

    /**
     * @return the ID of the entry
     */
    public int getQuark() {
        return fQuark;
    }

    /**
     * @return the state system which contains the counter
     */
    public ITmfStateSystem getStateSystem() {
        return fStateSystem;
    }

    /**
     * @return the slash-separated path of the entry in the state system
     */
    public @NonNull String getFullPath() {
        return fFullPath;
    }

}
