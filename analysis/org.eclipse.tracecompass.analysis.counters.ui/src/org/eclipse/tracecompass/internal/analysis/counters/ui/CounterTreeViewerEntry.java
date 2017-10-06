/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.counters.ui;

import org.eclipse.tracecompass.analysis.counters.core.CounterEntryModel;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfGenericTreeEntry;

/**
 * Entry of a TMF tree viewer which is associated to a counter.
 *
 * @author Matthew Khouzam
 * @author Mikael Ferland
 */
public class CounterTreeViewerEntry extends TmfGenericTreeEntry<CounterEntryModel> {

    /**
     * Constructor
     *
     * @param entry
     *            the Counter entry model that this Viewer entry encapsulates
     */
    public CounterTreeViewerEntry(CounterEntryModel entry) {
        super(entry);
    }
}
