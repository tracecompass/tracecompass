/**********************************************************************
 * Copyright (c) 2016, 2020 École Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.memory;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.common.core.format.DataSizeWithUnitFormat;
import org.eclipse.tracecompass.internal.provisional.tmf.ui.viewers.xychart.BaseXYPresentationProvider;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.linechart.TmfFilteredXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.linechart.TmfXYChartSettings;
import org.eclipse.tracecompass.tmf.ui.views.xychart.TmfChartView;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Memory usage view
 *
 * @since 2.2
 * @author Samuel Gagnon
 * @author Mahdi Zolnouri
 * @author Wassim Nasrallah
 */
public class MemoryUsageView2 extends TmfChartView {
   private final String fProviderId;
   private final TmfXYChartSettings fSettings;

    /**
     * Constructor
     *
     * @param title
     *            the Memory view's name.
     * @param providerId
     *            the ID of the provider to use for this view.
     * @param settings
     *            See {@link TmfXYChartSettings} to know what it contains
     */
    public MemoryUsageView2(String title, String providerId, TmfXYChartSettings settings) {
        super(title);
        fProviderId = providerId;
        fSettings = settings;
    }

    @Override
    protected TmfXYChartViewer createChartViewer(Composite parent) {
        TmfFilteredXYChartViewer viewer = new TmfFilteredXYChartViewer(parent, fSettings, fProviderId) {
            @Override
            public @NonNull OutputElementStyle getSeriesStyle(Long seriesId) {
                return getPresentationProvider().getSeriesStyle(seriesId);            }

            @Override
            protected BaseXYPresentationProvider createPresentationProvider(ITmfTrace trace) {
                return MemoryPresentationProvider.getForTrace(trace);
            }

        };
        viewer.getSwtChart().getAxisSet().getYAxis(0).getTick().setFormat(DataSizeWithUnitFormat.getInstance());
        return viewer;
    }

    @Override
    protected @NonNull TmfViewer createLeftChildViewer(Composite parent) {
        return new MemoryUsageTreeViewer2(parent, fProviderId);
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);

        // Add a tool bar button to filter active threads.
        getViewSite().getActionBars().getToolBarManager().appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, getFilterAction());
    }

    private Action getFilterAction() {
        Action action = new Action(Messages.MemoryView_FilterAction_Text, IAction.AS_CHECK_BOX) {
            // memory view is filtered by default.
            private boolean isFiltered = true;

            @Override
            public void run() {
                isFiltered ^= true;
                setToolTipText(isFiltered ? Messages.MemoryView_FilterAction_FilteredTooltipText : Messages.MemoryView_FilterAction_UnfilteredTooltipText);
                TmfViewer tree = getLeftChildViewer();
                if (tree instanceof MemoryUsageTreeViewer2) {
                    MemoryUsageTreeViewer2 memoryUsageTreeViewer = (MemoryUsageTreeViewer2) tree;
                    memoryUsageTreeViewer.setFiltered(isFiltered);
                }
            }
        };
        action.setToolTipText(Messages.MemoryView_FilterAction_FilteredTooltipText);
        action.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.tracecompass.tmf.ui", "icons/elcl16/filter_items.gif")); //$NON-NLS-1$ //$NON-NLS-2$
        // filtered by default, to not change the default behavior
        action.setChecked(true);
        return action;
    }

}
