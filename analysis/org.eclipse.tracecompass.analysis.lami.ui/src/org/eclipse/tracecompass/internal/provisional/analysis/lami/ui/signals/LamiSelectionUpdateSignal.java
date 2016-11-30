/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Jonathan Rajotte-Julien
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.signals;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.views.LamiReportViewTabPage;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignal;

/**
 * Enable signal sending on selection inside a LamiViewer implementation.
 *
 * @author Jonathan Rajotte-Julien
 */
public class LamiSelectionUpdateSignal extends TmfSignal {

    private final Set<Integer> fEntryIndexes;

    /**
     * Use the {@link LamiReportViewTabPage LamiReportViewTabPage} object as the
     * unique key for LAMI internal signaling. The {@link LamiReportViewTabPage}
     * object is also the synchronization point for LAMI internal signaling.
     */
    private final LamiReportViewTabPage fSignalKey;

    /**
     * Constructor for a new signal.
     *
     * @param source
     *            The object sending this signal
     * @param entryIndexList
     *            The list of selected indices
     * @param signalKey
     *            The {@link LamiReportViewTabPage LamiReportViewTabPage} acting
     *            as a key for the signal.
     */
    public LamiSelectionUpdateSignal(Object source, Set<Integer> entryIndexList, LamiReportViewTabPage signalKey) {
        super(source);
        fEntryIndexes = new HashSet<>(entryIndexList);
        fSignalKey = signalKey;
    }


    @Override
    public String toString() {
        return "[" + this.getClass().getSimpleName() + " (" + fEntryIndexes + ")]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * Getter for the entryIndex
     *
     * @return
     *          The new selected entry
     */
    public Set<Integer> getEntryIndex() {
        return fEntryIndexes;
    }


    /**
     * Getter for the exclusivity key
     *
     * @return
     *          The exclusivity key
     */
    public LamiReportViewTabPage getSignalKey() {
        return fSignalKey;
    }
}
