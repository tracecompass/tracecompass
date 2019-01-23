/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.views;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.analysis.lami.ui.Activator;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiAnalysisReport;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiResultTable;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.viewers.LamiTableViewer;
import org.eclipse.tracecompass.internal.tmf.ui.commands.ExportToTsvAction;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;
import org.eclipse.ui.IActionBars;

/**
 * Base view showing output of Babeltrace scripts.
 *
 * Implementations can specify which analysis modules to use, which will define
 * the scripts and parameters to use accordingly.
 *
 * @author Alexandre Montplaisir
 */
public final class LamiReportView extends TmfView {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /** View ID */
    public static final String VIEW_ID = "org.eclipse.tracecompass.analysis.lami.views.reportview"; //$NON-NLS-1$

    private final @Nullable LamiAnalysisReport fReport;
    private final List<LamiReportViewTabPage> fTabPages;

    private @Nullable CTabFolder fTabFolder;

    // ------------------------------------------------------------------------
    // Actions
    // ------------------------------------------------------------------------

    private class ToggleTableAction extends Action {
        @Override
        public void run() {
            LamiReportViewTabPage page = getCurrentSelectedPage();
            if (page == null) {
                return;
            }
            page.toggleTableViewer();
        }
    }

    private class NewCustomChartAction extends Action {

        @Override
        public void run() {
            LamiReportViewTabPage page = getCurrentSelectedPage();
            if (page == null) {
                return;
            }
            page.createNewCustomChart();
        }
    }


    private final Action fExportAction = new ExportToTsvAction() {
        @Override
        protected void exportToTsv(@Nullable OutputStream stream) {
            LamiReportViewTabPage tabPage = getCurrentSelectedPage();
            if (tabPage == null) {
                return;
            }
            LamiViewerControl viewerControl = tabPage.getTableViewerControl();
            TmfViewer viewer = viewerControl.getViewer();
            if (viewer instanceof LamiTableViewer) {
                ((LamiTableViewer) viewer).exportToTsv(stream);
            }
        }

        @Override
        protected @Nullable Shell getShell() {
            return getViewSite().getShell();
        }

    };

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     */
    public LamiReportView() {
        super(VIEW_ID);
        fReport = LamiReportViewFactory.getCurrentReport();
        fTabPages = new ArrayList<>();
    }

    // ------------------------------------------------------------------------
    // ViewPart
    // ------------------------------------------------------------------------

    @Override
    public void createPartControl(@Nullable Composite parent) {
        LamiAnalysisReport report = fReport;
        if (report == null || parent == null) {
            return;
        }

        setPartName(report.getName());

        fTabFolder = new CTabFolder(parent, SWT.NONE);
        fTabFolder.setSimple(false);

        for (LamiResultTable table : report.getTables()) {
            String name = table.getTableClass().getTableTitle();

            CTabItem tabItem = new CTabItem(fTabFolder, SWT.NULL);
            tabItem.setText(name);

            SashForm sf = new SashForm(fTabFolder, SWT.NONE);
            fTabPages.add(new LamiReportViewTabPage(sf, table));
            tabItem.setControl(sf);
        }

        /* Add toolbar buttons */
        Action toggleTableAction = new ToggleTableAction();
        toggleTableAction.setText(Messages.LamiReportView_ActivateTableAction_ButtonName);
        toggleTableAction.setToolTipText(Messages.LamiReportView_ActivateTableAction_ButtonTooltip);
        toggleTableAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath("icons/table.gif")); //$NON-NLS-1$

        IActionBars actionBars = getViewSite().getActionBars();
        IToolBarManager toolbarMgr = actionBars.getToolBarManager();
        toolbarMgr.add(toggleTableAction);

        IMenuManager menuMgr = actionBars.getMenuManager();

        IAction newChartAction = new NewCustomChartAction();
        newChartAction.setText(Messages.LamiReportView_NewCustomChart);

        IAction clearCustomViewsAction = new Action() {
            @Override
            public void run() {
                LamiReportViewTabPage tabPage = getCurrentSelectedPage();
                if (tabPage == null) {
                    return;
                }
                tabPage.clearAllCustomViewers();
                tabPage.getControl().layout();
            }
        };
        clearCustomViewsAction.setText(Messages.LamiReportView_ClearAllCustomViews);

        menuMgr.add(newChartAction);
        menuMgr.add(new Separator());
        menuMgr.add(clearCustomViewsAction);
        menuMgr.add(new Separator());
        menuMgr.add(fExportAction);

        /* Select the first tab initially */
        CTabFolder tf = checkNotNull(fTabFolder);
        if (tf.getItemCount() > 0) {
            tf.setSelection(0);
        }
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void setFocus() {
        if (fTabFolder != null) {
            fTabFolder.setFocus();
        }
    }

    @Nullable LamiReportViewTabPage getCurrentSelectedPage() {
        CTabFolder tf = fTabFolder;
        if (tf == null) {
            return null;
        }
        int idx = tf.getSelectionIndex();
        return fTabPages.get(idx);
    }

}
