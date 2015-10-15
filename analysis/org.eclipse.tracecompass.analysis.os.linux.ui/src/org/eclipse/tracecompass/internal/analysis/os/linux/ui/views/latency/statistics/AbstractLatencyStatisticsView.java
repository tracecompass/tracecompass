/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency.statistics;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;

/**
 * Abstract view to to be extended to display latency statistics.
 *
 * @author Bernd Hufmann
 *
 */
public abstract class AbstractLatencyStatisticsView extends TmfView {

    @Nullable private AbstractLatencyStatisticsViewer fStatsViewer = null;

    /**
     * Constructor
     */
    public AbstractLatencyStatisticsView() {
        super("StatisticsView"); //$NON-NLS-1$
    }

    @Override
    public void createPartControl(@Nullable Composite parent) {
        super.createPartControl(parent);
        AbstractLatencyStatisticsViewer statsViewer = createLatencyStatisticsViewer(NonNullUtils.checkNotNull(parent));
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null) {
            statsViewer.loadTrace(trace);
        }
        fStatsViewer = statsViewer;
    }

    @Override
    public void setFocus() {
        AbstractLatencyStatisticsViewer statsViewer = fStatsViewer;
        if (statsViewer != null) {
            statsViewer.getControl().setFocus();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        AbstractLatencyStatisticsViewer statsViewer = fStatsViewer;
        if (statsViewer != null) {
            statsViewer.dispose();
        }
    }

    /**
     * Creates a latency statistics viewer instance.
     *
     * @param parent
     *            the parent composite to create the viewer in.
     * @return the latency statistics viewer implementation
     */
    protected abstract AbstractLatencyStatisticsViewer createLatencyStatisticsViewer(Composite parent);

}
