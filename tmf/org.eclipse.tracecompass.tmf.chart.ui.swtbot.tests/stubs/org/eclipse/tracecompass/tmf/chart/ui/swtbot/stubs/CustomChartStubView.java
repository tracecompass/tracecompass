/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.chart.ui.swtbot.stubs;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartData;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartModel;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.ui.chart.IChartViewer;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.ui.dialog.ChartMakerDialog;
import org.eclipse.tracecompass.tmf.chart.core.model.IDataChartProvider;
import org.eclipse.tracecompass.tmf.chart.core.tests.stubs.StubObject;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;

/**
 * A stub view to test custom charts
 *
 * @author Geneviève Bastien
 */
@NonNullByDefault
public class CustomChartStubView extends TmfView {

    /**
     * ID of the stub view
     */
    public static final String ID = "org.eclipse.tracecompass.tmf.chart.ui.swtbot.view.stub";
    /**
     * Text for the menu item to create a custom chart
     */
    public static final String MENU_TITLE = "Create custom chart";

    private static final String VIEW_NAME = "Stub View for Chart Tests";

    private static @Nullable IDataChartProvider<StubObject> sfChartProvider = null;

    private @Nullable Composite fContainer;

    /**
     * Set the data chart provider that should be used for the chart maker
     * dialog
     *
     * @param provider The data chart provider
     */
    public static void setChartProvider(IDataChartProvider<StubObject> provider) {
        sfChartProvider = provider;
    }

    /**
     * Constructor
     */
    public CustomChartStubView() {
        super(VIEW_NAME);
    }

    @Override
    public void setFocus() {

    }

    @Override
    public void createPartControl(@Nullable Composite parent) {
        super.createPartControl(parent);
        SashForm sf = new SashForm(parent, SWT.NONE);
        fContainer = sf;

        /* Add a menu for adding charts */
        Action addChart = new NewChartAction();
        addChart.setText(MENU_TITLE);

        IMenuManager menuMgr = getViewSite().getActionBars().getMenuManager();
        menuMgr.add(addChart);
    }

    /**
     * Method that returns the composite to put a chart in
     *
     * @return The composite to put more widget
     */
    private @Nullable Composite getContainer() {
        return fContainer;
    }

    // ------------------------------------------------------------------------
    // Anonymous classes
    // ------------------------------------------------------------------------

    private class NewChartAction extends Action {
        @Override
        public void run() {

            /* Get the composite for putting the chart */
            Composite composite = CustomChartStubView.this.getContainer();
            if (composite == null) {
                return;
            }

            /* Open the chart maker dialog */
            IDataChartProvider<StubObject> chartProvider = sfChartProvider;
            if (chartProvider == null) {
                throw new NullPointerException("no chart provider set. Need to call CustomChartStubView.setChartProvider() before clicking the menu item");
            }
            ChartMakerDialog dialog = new ChartMakerDialog(NonNullUtils.checkNotNull(composite.getShell()), chartProvider);
            if (dialog.open() != Window.OK) {
                return;
            }

            /* Make sure the data for making a chart was generated */
            ChartData data = dialog.getDataSeries();
            ChartModel model = dialog.getChartModel();
            if (data == null || model == null) {
                return;
            }

            /* Make a chart with the factory constructor */
            IChartViewer.createChart(composite, data, model);

        }
    }

}
