/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   France Lapointe Nguyen - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.tracecompass.analysis.os.linux.core.latency.LatencyAnalysis;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.AbstractSegmentStoreAnalysisModule;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.AbstractSegmentStoreTableViewer;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency.statistics.LatencyStatisticsView;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * Displays the latency analysis data in a column table
 *
 * @author France Lapointe Nguyen
 */
public class LatencyTableViewer extends AbstractSegmentStoreTableViewer {

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param tableViewer
     *            The table viewer
     */
    public LatencyTableViewer(TableViewer tableViewer) {
        super(tableViewer);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    protected @Nullable AbstractSegmentStoreAnalysisModule getSegmentStoreAnalysisModule(ITmfTrace trace) {
          return TmfTraceUtils.getAnalysisModuleOfClass(trace, LatencyAnalysis.class, LatencyAnalysis.ID);
    }

    @Override
    protected void appendToTablePopupMenu(IMenuManager manager, IStructuredSelection sel) {
        super.appendToTablePopupMenu(manager, sel);
        manager.add(new Separator());
        IAction showStatisticsAction = new Action(Messages.LatencyTable_ShowStatisticsActionName) {
            @Override
            public void run() {
                final IWorkbench wb = PlatformUI.getWorkbench();
                final IWorkbenchPage activePage = wb.getActiveWorkbenchWindow().getActivePage();
                try {
                    activePage.showView(LatencyStatisticsView.ID);
                } catch (PartInitException e) {
                }
            }
        };

       manager.add(showStatisticsAction);
    }
}
