/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.counters.ui;

import java.util.UUID;

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
    private final String fFullPath;
    private final UUID fTraceID;

    /**
     * Constructor
     *
     * @param quark
     *            ID of the entry
     * @param name
     *            Name of this entry
     * @param fullPath
     *            Slash-separated path of the entry in the state system
     * @param traceID
     *            The ID of the trace associated to this entry
     */
    public CounterTreeViewerEntry(int quark, String name, @NonNull String fullPath, UUID traceID) {
        super(name);
        fQuark = quark;
        fFullPath = fullPath;
        fTraceID = traceID;
    }

    /**
     * Gets the quark associated with this entry
     *
     * @return The quark of the entry
     */
    public int getQuark() {
        return fQuark;
    }

    /**
     * Gets the slash-separated path of the entry in the state system
     *
     * @return The full path of this entry
     */
    public String getFullPath() {
        return fFullPath;
    }

    /**
     * Gets the trace's ID of this entry
     *
     * @return The ID of the trace associated to this entry
     */
    public UUID getTraceID() {
        return fTraceID;
    }
}
