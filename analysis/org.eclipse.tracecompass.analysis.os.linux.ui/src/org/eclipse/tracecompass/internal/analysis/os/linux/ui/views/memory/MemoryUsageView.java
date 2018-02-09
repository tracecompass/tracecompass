/**********************************************************************
 * Copyright (c) 2016, 2018 École Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/
package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.memory;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.os.linux.core.memory.MemoryUsageTreeModel;
import org.eclipse.tracecompass.common.core.format.DataSizeWithUnitFormat;
import org.eclipse.tracecompass.internal.provisional.tmf.core.presentation.IYAppearance;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfFilteredXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfXYChartSettings;
import org.eclipse.tracecompass.tmf.ui.views.TmfChartView;
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
@SuppressWarnings("restriction")
public class MemoryUsageView extends TmfChartView {
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
    public MemoryUsageView(String title, String providerId, TmfXYChartSettings settings) {
        super(title);
        fProviderId = providerId;
        fSettings = settings;
    }

    @Override
    protected TmfXYChartViewer createChartViewer(Composite parent) {
        TmfFilteredXYChartViewer viewer = new TmfFilteredXYChartViewer(parent, fSettings, fProviderId) {
            @Override
            public @NonNull IYAppearance getSeriesAppearance(String seriesName) {
                int width = seriesName.endsWith(MemoryUsageTreeModel.TOTAL_SUFFIX) ? 2 : 1;
                return getPresentationProvider().getAppearance(seriesName, IYAppearance.Type.LINE, width);
            }
        };
        viewer.getSwtChart().getAxisSet().getYAxis(0).getTick().setFormat(DataSizeWithUnitFormat.getInstance());
        return viewer;
    }

    @Override
    protected @NonNull TmfViewer createLeftChildViewer(Composite parent) {
        return new MemoryUsageTreeViewer(parent, fProviderId);
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
                if (tree instanceof MemoryUsageTreeViewer) {
                    MemoryUsageTreeViewer memoryUsageTreeViewer = (MemoryUsageTreeViewer) tree;
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
