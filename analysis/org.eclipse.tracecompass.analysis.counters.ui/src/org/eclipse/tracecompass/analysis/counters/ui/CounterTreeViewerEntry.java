/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.counters.ui;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeViewerEntry;

/**
 * Entry of a TMF tree viewer which is associated to a counter.
 *
 * @author Matthew Khouzam
 * @author Mikael Ferland
 */
public class CounterTreeViewerEntry extends TmfTreeViewerEntry {

    private final int fQuark;

    /**
     * Constructor
     *
     * @param name
     *            Name of the entry
     * @param quark
     *            ID of the entry
     */
    public CounterTreeViewerEntry(@NonNull String name, int quark) {
        super(name);
        fQuark = quark;
    }

    /**
     * @return the ID of the entry
     */
    public int getQuark() {
        return fQuark;
    }

}
