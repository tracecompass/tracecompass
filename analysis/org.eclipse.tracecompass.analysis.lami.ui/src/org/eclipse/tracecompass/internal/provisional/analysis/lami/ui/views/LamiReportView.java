/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.views;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

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
import org.eclipse.tracecompass.tmf.ui.viewers.IImageSave;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.eclipse.tracecompass.tmf.ui.views.SaveImageUtil;
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

    private static final Separator SEPARATOR = new Separator();

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

    private final Action fExportTsvAction = new ExportToTsvAction() {
        @Override
        protected void exportToTsv(@Nullable OutputStream stream) {
            TmfViewer viewer = getViewer();
            if (viewer instanceof LamiTableViewer) {
                ((LamiTableViewer) viewer).exportToTsv(stream);
            }
        }

        private @Nullable TmfViewer getViewer() {
            LamiReportViewTabPage tabPage = getCurrentSelectedPage();
            if (tabPage == null) {
                return null;
            }
            LamiViewerControl viewerControl = tabPage.getTableViewerControl();
            TmfViewer viewer = viewerControl.getViewer();
            return viewer;
        }

        @Override
        public boolean isEnabled() {
            return (getViewer() instanceof LamiTableViewer);
        }

        @Override
        protected @Nullable Shell getShell() {
            return getViewSite().getShell();
        }
    };

    private final Function<Integer, @Nullable IImageSave> fImageProvider = index -> {
        LamiReportViewTabPage selectedPage = getCurrentSelectedPage();
        if (selectedPage == null) {
            return null;
        }
        List<LamiViewerControl> plots = selectedPage.getCustomGraphViewerControls();

        if (index >= 0 && index < plots.size()) {
            TmfViewer viewer = plots.get(index).getViewer();
            if (viewer instanceof IImageSave) {
                return (IImageSave) viewer;
            }
        }
        return null;
    };

    private List<Supplier<@Nullable IImageSave>> getSuppliers(){
        List<Supplier<@Nullable IImageSave>> suppliers = new ArrayList<>();
        LamiReportViewTabPage selectedPage = getCurrentSelectedPage();
        if (selectedPage == null) {
            return Collections.emptyList();
        }
        List<LamiViewerControl> plots = selectedPage.getCustomGraphViewerControls();
        for(int i = 0; i < plots.size(); i++ ) {
            IImageSave iis = fImageProvider.apply(i);
            if(iis != null) {
                suppliers.add(()->iis);
            }
        }
        return suppliers;

    }

    private IAction fClearCustomViewsAction = new Action() {
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

    private IAction fNewChartAction = new NewCustomChartAction();

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

        fNewChartAction.setText(Messages.LamiReportView_NewCustomChart);

        fClearCustomViewsAction.setText(Messages.LamiReportView_ClearAllCustomViews);
        IMenuManager menuMgr = actionBars.getMenuManager();
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener((@Nullable IMenuManager manager) -> {
            if (manager != null) {
                populateMenu(manager);
            }
        });
        populateMenu(menuMgr);

        /* Select the first tab initially */
        CTabFolder tf = checkNotNull(fTabFolder);
        if (tf.getItemCount() > 0) {
            tf.setSelection(0);
        }

    }

    private void populateMenu(IMenuManager menuMgr) {
        menuMgr.add(fNewChartAction);
        menuMgr.add(SEPARATOR);
        menuMgr.add(fClearCustomViewsAction);
        menuMgr.add(SEPARATOR);
        if(fExportTsvAction.isEnabled()) {
            menuMgr.add(fExportTsvAction);
        }
        List<Supplier<@Nullable IImageSave>> suppliers = getSuppliers();
        boolean isSingleton = suppliers.size()==1;
        LamiReportViewTabPage currentSelectedPage = getCurrentSelectedPage();
        if (currentSelectedPage != null) {
            for (int index = 0; index < suppliers.size(); index++) {
                String fileName = isSingleton ? currentSelectedPage.getName() : String.format("%s%02d", currentSelectedPage.getName(), index + 1); //$NON-NLS-1$
                IAction action = SaveImageUtil.createSaveAction(fileName, suppliers.get(index));
                String suffix = isSingleton ? "" : (" " + (index + 1)); //$NON-NLS-1$ //$NON-NLS-2$
                action.setText(Messages.LamiReportView_ActivateTableAction_ExportChart + suffix + '…');
                menuMgr.add(action);
            }
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
        if (idx != -1) {
            return fTabPages.get(idx);
        }
        return null;
    }

}
